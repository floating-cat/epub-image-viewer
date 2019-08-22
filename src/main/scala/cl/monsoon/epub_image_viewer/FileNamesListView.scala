package cl.monsoon.epub_image_viewer

import org.scalajs.dom.File
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.{className, li, ul}

@react object FileNamesListView {
  type Props = Seq[File]

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val lis = props.map(file => li(className := "list-group-item")(file.name))
    ul(className := "list-group")(lis)
  }
}
