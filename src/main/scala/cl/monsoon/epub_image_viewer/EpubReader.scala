package cl.monsoon.epub_image_viewer

import cats.data.NonEmptyChain
import zio.ZIO

trait EpubReader[File] {
  type FilePath = String
  type ImageFilePath = String
  type ImageFile = File
  type FileSupplier = FilePath => Option[File]
  type Error = NonEmptyChain[String]
  type FileReader[A] = ZIO[FileSupplier, Error, A]

  // This is a simple parse method, we don't validate the conformance of
  // this EPUB strictly. Of course this is not this programs responsibility.
  def parse: FileReader[Seq[ImageFilePath]]
}
