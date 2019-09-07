package cl.monsoon.epub_image_viewer

trait EpubReader[F[_]] {
  // We don't validate the conformance of this EPUB strictly when we read this EPUB,
  // Of course this is not this program's responsibility.
  def getImageDataUrl: F[_]
}
