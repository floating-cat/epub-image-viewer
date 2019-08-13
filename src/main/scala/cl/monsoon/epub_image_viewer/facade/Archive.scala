package cl.monsoon.epub_image_viewer.facade

import org.scalajs.dom.File

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("libarchive.js/main.js", "Archive")
@js.native
class Archive(@unused file: File, @unused options: Options) extends js.Object {
  def open(): js.Promise[Archive] = js.native

  def extractFiles(): js.Promise[js.Dynamic] = js.native
}

trait Options extends js.Object {
  def workerUrl: String
}
