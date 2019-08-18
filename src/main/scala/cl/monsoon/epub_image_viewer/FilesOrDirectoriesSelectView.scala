package cl.monsoon.epub_image_viewer

import cats.Show
import cats.data.NonEmptyChain
import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.{Errors, ImageFileDataUrl}
import cl.monsoon.epub_image_viewer.facade.Archive
import cl.monsoon.epub_image_viewer.util.CustomAttributeUtil.webkitdirectory
import cl.monsoon.epub_image_viewer.util.DomImplicit._
import cl.monsoon.epub_image_viewer.util.SortUtil
import cl.monsoon.epub_image_viewer.util.ZIOImplicit._
import org.scalajs.dom.console.log
import org.scalajs.dom.{File, window}
import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._
import zio.{DefaultRuntime, IO, UIO, ZIO}

import scala.util.chaining._

@react object FilesOrDirectoriesSelectView {
  type Props = Seq[ImageFileDataUrl] => Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] {
    imageFileDataUrlsCallback =>
      input(
        `type` := "file",
        webkitdirectory := "true",
        multiple,
        onChange := { e =>
          val epubFiles = e.target.files.toVector
            .filter(_.name.endsWith(".epub"))
            .pipe(files => SortUtil.sortForASeriesThings(files)(Show.show(_.name)))
            .toVector

          epubFiles
            .traverse(getEpubFileImageDataUrls)
            .flatMap(
              imageDataUrls =>
                UIO.effectTotal {
                  imageFileDataUrlsCallback(imageDataUrls.flatten)
                }
            )
            .flatMapError(o => {
              window.alert(o.mkString_("", "\n", ""))
              ZIO.none
            })
            .pipe(new DefaultRuntime {}.unsafeRunAsync_(_))
        }
      )
  }

  def getEpubFileImageDataUrls(epubFile: File): IO[Errors, Seq[ImageFileDataUrl]] =
    ZIO
      .effectTotal(log(epubFile))
      .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
      .mapError[Errors](
        e => NonEmptyChain(s"Can't load ${epubFile.name}: " + e.getMessage)
      )
      .flatMap(new EpubReaderJs().getImageDataUrl.provide)
}
