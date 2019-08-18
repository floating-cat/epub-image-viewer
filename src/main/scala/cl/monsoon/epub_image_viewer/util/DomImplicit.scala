package cl.monsoon.epub_image_viewer.util

import org.scalajs.dom.{File, FileList}
import org.scalajs.dom.ext.EasySeq

object DomImplicit {
  implicit class PimpedFileList(fileList: FileList)
      extends EasySeq[File](fileList.length, fileList.apply)
}
