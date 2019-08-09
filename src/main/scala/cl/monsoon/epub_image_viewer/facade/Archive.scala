package cl.monsoon.epub_image_viewer.facade

import org.scalajs.dom.File

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("libarchive.js/main.js", "Archive")
@js.native
class Archive(file: File, options: Options) extends js.Object {
  def open(): js.Promise[Archive] = js.native

  def extractFiles(): js.Promise[js.Dictionary[File]] = js.native
}

trait Options extends js.Object {
  def workerUrl: String
}
