package cl.monsoon.epub_image_viewer

import org.scalajs.dom.document
import org.scalatest.FunSuite
import slinky.web.ReactDOM

class AppTest extends FunSuite {
  test("Renders without crashing") {
    val div = document.createElement("div")
    ReactDOM.render(App(), div)
    ReactDOM.unmountComponentAtNode(div)
  }
}
