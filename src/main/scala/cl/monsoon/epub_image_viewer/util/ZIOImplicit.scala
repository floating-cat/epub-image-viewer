package cl.monsoon.epub_image_viewer.util

import cats.Applicative
import zio.ZIO

// workaround  https://github.com/zio/interop-cats/pull/28 temporarily
object ZIOImplicit {
  implicit def zioApplicativeInstance[R, E]: Applicative[({ type Z[A] = ZIO[R, E, A] })#Z] =
    new Applicative[({ type Z[A] = ZIO[R, E, A] })#Z] {
      override def pure[A](x: A): ZIO[R, E, A] =
        ZIO.succeed(x)

      override def ap[A, B](ff: ZIO[R, E, A => B])(fa: ZIO[R, E, A]): ZIO[R, E, B] =
        ff.flatMap(a => fa.map(a))
    }
}
