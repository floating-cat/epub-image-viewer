package cl.monsoon.epub_image_viewer

import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.ImageFileDataUrl
import cl.monsoon.epub_image_viewer.facade.Archive
import org.scalajs.dom.console.log
import org.scalajs.dom.document
import org.scalajs.dom.raw.KeyboardEvent
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._
import zio.{DefaultRuntime, ZIO}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.chaining._

@react object App {
  type Props = Unit

  @unused
  // we need this in order to provide css styling
  private val css = AppCSS

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    val (imageFileDataUrls, imageFileDataUrlsUpdateState) = useState(none[Seq[ImageFileDataUrl]])
    val (imageViewedIndex, imageViewedIndexUpdateState) = useState(0)
    val (autoFitImage, autoFitImageUpdateState) = useState(true)

    useEffect { () =>
      // Use js function explicit because of  https://stackoverflow.com/q/57148965/2331527
      val keyDownListener: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
        imageFileDataUrls.foreach { urls =>
          e.key match {
            case "ArrowLeft" =>
              if (urls.lengthIs >= imageViewedIndex + 1 + 1) {
                imageViewedIndexUpdateState(imageViewedIndex + 1)
              }
            case "ArrowRight" =>
              if (imageViewedIndex - 1 >= 0) {
                imageViewedIndexUpdateState(imageViewedIndex - 1)
              }
            case "f" => autoFitImageUpdateState(!_)
            case _ =>
          }
        }
      }

      val eventName = "keydown"
      document.addEventListener(eventName, keyDownListener)
      () => document.removeEventListener(eventName, keyDownListener)
    }

    val mainContent = if (imageFileDataUrls.isEmpty || imageFileDataUrls.get.isEmpty) {
      input(
        `type` := "file",
        onChange := { e =>
          val epubFile = e.target.files(0)
          ZIO
            .effectTotal(log(epubFile))
            .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
            .flatMap(new EpubReaderJs().getImageDataUrl.provide)
            .flatMap(
              imageDataUrls =>
                ZIO.effect {
                  imageFileDataUrlsUpdateState(Some(imageDataUrls))
                  imageViewedIndexUpdateState(0)
                }
            )
            .pipe(new DefaultRuntime {}.unsafeRunAsync_(_))
        }
      )
    } else {
      img(
        className := "illustration" + (if (autoFitImage) " auto_fit" else ""),
        src := imageFileDataUrls.get(imageViewedIndex)
      )
    }

    div(className := "App")(mainContent)
  }
}

@JSImport("resources/App.css", JSImport.Default)
@js.native
object AppCSS extends js.Object
