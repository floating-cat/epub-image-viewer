package cl.monsoon.epub_image_viewer

import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.ImageFileDataUrl
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

@react object App {
  type Props = Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    val (imageFileDataUrls, imageFileDataUrlsUpdateState) = useState(none[Seq[ImageFileDataUrl]])

    val mainContent = if (imageFileDataUrls.isEmpty || imageFileDataUrls.get.isEmpty) {
      FilesOrDirectoriesInputView(
        (imageFileDataUrlsUpdateState(_: Option[Seq[ImageFileDataUrl]])).compose(Some(_))
      )
    } else {
      IllustrationDisplayView((imageFileDataUrls.get, imageFileDataUrlsUpdateState))
    }

    div(className := "App container")(mainContent)
  }
}
