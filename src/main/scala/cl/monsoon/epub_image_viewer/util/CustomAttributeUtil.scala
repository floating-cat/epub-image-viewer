package cl.monsoon.epub_image_viewer.util

import slinky.core.CustomAttribute

object CustomAttributeUtil {
  val webkitdirectory = new CustomAttribute[String]("webkitdirectory")

  val role = new CustomAttribute[String]("role")
  val ariaLabel = new CustomAttribute[String]("aria-label")
}
