package cl.monsoon.epub_image_viewer

import cl.monsoon.epub_image_viewer.facade.{Archive, Options}

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

@react object App {
  type Props = Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    div(className := "App")(
      input(
        `type` := "file",
        onChange := (e => {
          dom.console.log(e.target.files(0))
          val archive = new Archive(e.target.files(0), new Options {
            override def workerUrl: String = "worker-bundle.js"
          })

          archive
            .open()
            .toFuture
            .flatMap(_.extractFiles().toFuture)
            .foreach(o => dom.console.log(o))
        })
      )
    )
  }
}
