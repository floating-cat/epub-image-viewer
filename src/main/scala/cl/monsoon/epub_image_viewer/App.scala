package cl.monsoon.epub_image_viewer

import cl.monsoon.epub_image_viewer.facade.Archive
import org.scalajs.dom.console.log
import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._
import zio.{DefaultRuntime, ZIO}

import scala.util.chaining._

@react object App {
  type Props = Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    div(className := "App")(
      input(
        `type` := "file",
        onChange := (e => {
          val epubFile = e.target.files(0)
          ZIO
            .effectTotal(log(epubFile))
            .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
            .flatMap(new EpubReaderJs().parse.provide)
            .flatMap(files => ZIO.effectTotal(files.foreach(log(_))))
            .pipe(new DefaultRuntime {}.unsafeRunAsync_(_))
        })
      )
    )
  }
}
