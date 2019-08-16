package cl.monsoon.epub_image_viewer

import cats.data._
import cats.implicits._
import cl.monsoon.epub_image_viewer.facade.DOMException
import cl.monsoon.epub_image_viewer.util.ZIOImplicit._
import org.scalajs.dom.ext._
import org.scalajs.dom.{DOMParser, Document, File}
import zio.{IO, ZIO}

import scala.scalajs.js

final class EpubReaderJs extends EpubReader[File] {

  val containerXmlFilePath = "META-INF/container.xml"

  override def parse: FileReader[Seq[ImageFilePath]] =
    getContentOpfFile
      .flatMap(getSpineDocuments)
      .flatMap(getImageElements)

  private def getContentOpfFile: FileReader[FilePath] =
    getFileText(containerXmlFilePath).flatMap { text =>
      parseXml(text)
        .getElementsByTagName("rootfile")
        .toSeq
        .find(
          e => e.getAttribute("media-type") == "application/oebps-package+xml"
        )
        .flatMap(e => Option(e.getAttribute("full-path")))
        .filter(_.nonEmpty)
        .fold[IO[Error, FilePath]](
          IO.fail(NonEmptyChain("Can't find content.opf in epub."))
        )(
          IO.succeed
        )
    }

  protected def getSpineDocuments(contentOpfPath: FilePath): FileReader[Seq[FilePath]] = {
    val fileParentRegex = "(.+/).+".r
    val fileParent = contentOpfPath match {
      case fileParentRegex(fileParentPath) => fileParentPath
      case _ => ""
    }

    getFileText(contentOpfPath).flatMap { text =>
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
        .traverse { e =>
          val idref = e.getAttribute("idref")
          itemMap(idref).toValidNec(s"Can't find $idref hrefs in spine documents.")
        }

      spineDocuments.fold(IO.fail, IO.succeed)
    }
  }

  private def getImageElements(documentPaths: Seq[FilePath]): FileReader[Seq[ImageFilePath]] =
    documentPaths.toVector
      .traverse(getFileTextWithPath)
      .flatMap { texts =>
        texts.flatMap { textWithDocumentPath =>
          parseXml(textWithDocumentPath._1)
            .getElementsByTagName("image")
            .toVector
            .map(
              e =>
                Option(e.getAttribute("xlink:href"))
                  .filter(_.nonEmpty)
                  .toValidNec(s"Can't find xlink:href in ${textWithDocumentPath._2} document.")
            )
        }.sequence
          .fold(IO.fail, IO.succeed)
      }

  private def getFile(filePath: FilePath): FileReader[File] =
    ZIO
      .access[FileSupplier](_(filePath))
      .someOrFail(NonEmptyChain(s"Can't find $filePath in this epub file"))

  private def getFileText(filePath: FilePath): FileReader[String] =
    getFile(filePath).flatMap { file =>
      val fileReader = new org.scalajs.dom.FileReader()
      val textIO = IO.effectAsync[Error, String] { callback =>
        fileReader.onloadend = _ =>
          if (fileReader.error != null) {
            callback(IO.succeed[String](fileReader.result.asInstanceOf[String]))
          } else {
            callback(
              IO.fail(
                NonEmptyChain(
                  s"Can't get $filePath content in epub: " +
                    s"${fileReader.asInstanceOf[DOMException].message}."
                )
              )
            )
          }
      }
      fileReader.readAsText(file)

      textIO
    }

  private def getFileTextWithPath(filePath: FilePath): FileReader[(String, String)] =
    getFileText(filePath).map((_, filePath))

  private def parseXml(text: String): Document = {
    val parser = new DOMParser()
    parser.parseFromString(text, "text/xml")
  }
}

object EpubReaderJs {
  def getFileSupplier(filesObject: js.Dynamic): String => Option[File] = { filePath =>
    val paths = if (filePath != null) {
      filePath.split("/").toList
    } else {
      List.empty
    }

    paths
      .foldM(filesObject) { (nestFilesObject, path) =>
        val nest = nestFilesObject.selectDynamic(path)
        Option.when(nest == null)(nest)
      }
      // TODO
      .map(_.asInstanceOf[File])
  }
}
