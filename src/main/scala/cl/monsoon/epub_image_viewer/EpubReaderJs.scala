package cl.monsoon.epub_image_viewer

import java.net.URI

import cats.data._
import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.{Errors, FilePath, ImageFileDataUrl, ImageFilePath}
import cl.monsoon.epub_image_viewer.facade.DOMException
import cl.monsoon.epub_image_viewer.util.ZIOImplicit._
import org.scalajs.dom.ext._
import org.scalajs.dom.{DOMParser, Document, Element, File}
import zio.{IO, ZIO}

final class EpubReaderJs extends EpubReader[File] {

  val containerXmlFilePath = "META-INF/container.xml"

  override def getImageDataUrl: FileReader[Seq[ImageFileDataUrl]] =
    getContentOpfFile
      .flatMap(getSpineDocuments)
      .flatMap(getImageElements)
      .flatMap(getImageFileDataUrl)

  private def getContentOpfFile: FileReader[FilePath] =
    getFileContent(containerXmlFilePath).flatMap { text =>
      parseXml(text)
        .getElementsByTagName("rootfile")
        .toSeq
        .find(
          e => e.getAttribute("media-type") == "application/oebps-package+xml"
        )
        .flatMap(e => Option(e.getAttribute("full-path")))
        .filter(_.nonEmpty)
        .fold[IO[Errors, FilePath]](
          IO.fail(NonEmptyChain("Can't find content.opf in epub."))
        )(
          IO.succeed
        )
    }

  private def getSpineDocuments(contentOpfPath: FilePath): FileReader[Seq[FilePath]] = {
    val fileParentRegex = "(.+/).+".r
    val fileParent = contentOpfPath match {
      case fileParentRegex(fileParentPath) => fileParentPath
      case _ => ""
    }

    getFileContent(contentOpfPath).flatMap { text =>
      val document = parseXml(text)
      val itemMap = document
        .getElementsByTagName("item")
        .toSeq
        .map(e => (e.getAttribute("id"), Option(e.getAttribute("href")).map(fileParent + _)))
        .toMap

      val spineDocuments = document
        .getElementsByTagName("itemref")
        // https://github.com/typelevel/cats/issues/1222
        .toVector
        .map(_.getAttribute("idref"))
        // remove this file because this file doesn't in the DeDRMed Kobo books
        .filterNot(_ == "kobo-locked.html")
        .traverse(
          idref => itemMap(idref).toValidNec(s"Can't find $idref hrefs in spine documents.")
        )

      spineDocuments.fold(IO.fail, IO.succeed)
    }
  }

  private def getImageElements(documentPaths: Seq[FilePath]): FileReader[Seq[ImageFilePath]] =
    documentPaths.toVector
      .traverse(getFileTextWithPath)
      .flatMap { texts =>
        texts.flatMap { textWithDocumentPath =>
          parseXml(textWithDocumentPath._1)
          // the reason why we don't use image[xlink:href] or img[src] here
          // is to fast fail code if these elements don't have this attribute
            .querySelectorAll("image, img")
            .toList
            .map { node =>
              // form MDN, the nodes returns from querySelectorAll all are elements
              val element = node.asInstanceOf[Element]
              Option(element.getAttribute("xlink:href"))
                .orElse(Option(element.getAttribute("src")))
                .filter(_.nonEmpty)
                // Scala.js doesn't support java Path, so use uri.
                // also see https://stackoverflow.com/a/10159309/2331527
                .map(new URI(textWithDocumentPath._2).resolve(".").resolve(_).toString)
                .toValidNec(
                  s"Can't find xlink:href or src property in ${textWithDocumentPath._2} document."
                )
            }
        }.sequence
          .fold(IO.fail, IO.succeed)
      }

  private def getImageFileDataUrl(
      imageFilePaths: Seq[ImageFilePath]
    ): FileReader[Seq[ImageFileDataUrl]] =
    imageFilePaths.toVector
      .traverse(getFileContent(_, Right("readAsDataUrl")))
      .map(_.toSeq)

  private def getFile(filePath: FilePath): FileReader[File] =
    ZIO
      .access[FileSupplier](_(filePath))
      .someOrFail(NonEmptyChain(s"Can't find $filePath in this epub file"))

  private def getFileContent(
      filePath: FilePath,
      // looks | doesn't work well with literal-based singleton types
      `type`: Either["readAsText", "readAsDataUrl"] = Left("readAsText")
    ): FileReader[String] =
    getFile(filePath).flatMap { file =>
      val fileReader = new org.scalajs.dom.FileReader()
      val content = IO.effectAsync[Errors, String] { callback =>
        fileReader.onloadend = _ =>
          if (fileReader.error == null) {
            callback(IO.succeed[String](fileReader.result.asInstanceOf[String]))
          } else {
            callback(
              IO.fail(
                NonEmptyChain(
                  s"Can't get $filePath content in epub: " +
                    s"${fileReader.error.asInstanceOf[DOMException].message}."
                )
              )
            )
          }
      }

      `type` match {
        case Left("readAsText") => fileReader.readAsText(file)
        case _ => fileReader.readAsDataURL(file)
      }

      content
    }

  private def getFileTextWithPath(filePath: FilePath): FileReader[(String, String)] =
    getFileContent(filePath).map((_, filePath))

  private def parseXml(text: String): Document = {
    val parser = new DOMParser()
    parser.parseFromString(text, "text/xml")
  }
}
