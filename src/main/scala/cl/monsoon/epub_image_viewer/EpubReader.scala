package cl.monsoon.epub_image_viewer

import cats.data.NonEmptyChain
import cl.monsoon.epub_image_viewer.EpubReader.{Errors, FilePath, ImageFileDataUrl}
import zio.ZIO

trait EpubReader[File] {
  type FileSupplier = FilePath => Option[File]
  type FileReader[A] = ZIO[FileSupplier, Errors, A]

  // We don't validate the conformance of this EPUB strictly when we read this EPUB,
  // Of course this is not this program's responsibility.
  def getImageDataUrl: FileReader[Seq[ImageFileDataUrl]]
}

object EpubReader {
  type FilePath = String
  type ImageFilePath = String
  type ImageFileDataUrl = String
  type Errors = NonEmptyChain[String]
}
