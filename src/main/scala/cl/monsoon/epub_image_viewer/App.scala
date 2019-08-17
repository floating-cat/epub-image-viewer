package cl.monsoon.epub_image_viewer

import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.ImageFileDataUrl
import cl.monsoon.epub_image_viewer.facade.Archive
import org.scalajs.dom.console.log
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._
import zio.{DefaultRuntime, ZIO}

import scala.util.chaining._

@react object App {
  type Props = Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    val (state, updateState) = useState(none[Seq[ImageFileDataUrl]])
    val mainContent = if (state.isEmpty || state.get.isEmpty) {
      input(
        `type` := "file",
        onChange := (e => {
          val epubFile = e.target.files(0)
          ZIO
            .effectTotal(log(epubFile))
            .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
            .flatMap(new EpubReaderJs().getImageDataUrl.provide)
            .flatMap(imageDataUrls => ZIO.effectTotal(updateState(Some(imageDataUrls))))
            .pipe(new DefaultRuntime {}.unsafeRunAsync_(_))
        })
      )
    } else {
      img(src := state.get(0))
    }

    div(className := "App")(mainContent)
  }
}
