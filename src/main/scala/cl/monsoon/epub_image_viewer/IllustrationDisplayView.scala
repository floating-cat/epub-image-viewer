package cl.monsoon.epub_image_viewer

import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReaderJs.ImageFileDataUrl
import org.scalajs.dom.document
import org.scalajs.dom.raw.KeyboardEvent
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.scalajs.js

@react object IllustrationDisplayView {
  type Props = (Seq[ImageFileDataUrl], Option[Seq[ImageFileDataUrl]] => Unit)

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val (imageViewedIndex, imageViewedIndexUpdateState) = useState(0)
    val (autoFitImage, autoFitImageUpdateState) = useState(true)

    useEffect { () =>
      // Use js function explicit because of  https://stackoverflow.com/q/57148965/2331527
      val keyDownListener: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
        e.key match {
          case "ArrowLeft" =>
            if (props._1.lengthIs >= imageViewedIndex + 1 + 1) {
              imageViewedIndexUpdateState(imageViewedIndex + 1)
            }
          case "ArrowRight" =>
            if (imageViewedIndex - 1 >= 0) {
              imageViewedIndexUpdateState(imageViewedIndex - 1)
            }
          case "f" =>
            autoFitImageUpdateState(!_)
          case "e" =>
            props._2(none)
            imageViewedIndexUpdateState(0)
          case _ =>
        }
      }

      val eventName = "keydown"
      document.addEventListener(eventName, keyDownListener)
      () => document.removeEventListener(eventName, keyDownListener)
    }

    img(
      className := "illustration" + (if (autoFitImage) " auto-fit" else ""),
      src := props._1(imageViewedIndex)
    )
  }
}
