enablePlugins(ScalaJSBundlerPlugin)

name := "epub-images-viewer"

scalaVersion := "2.13.0"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-P:scalajs:sjsDefinedByDefault",
  "-language:reflectiveCalls"
)

libraryDependencies ++= Seq(
  "me.shadaj" %%% "slinky-web" % "0.6.2",
  "me.shadaj" %%% "slinky-hot" % "0.6.2",
  "org.scala-js" %%% "scalajs-dom" % "0.9.7",
  "org.typelevel" %%% "cats-core" % "2.0.0-RC1",
  "org.typelevel" %%% "cats-effect" % "2.0.0-RC1",
  "dev.zio" %%% "zio" % "1.0.0-RC11-1",
  "dev.zio" %%% "zio-interop-cats" % "2.0.0.0-RC2",
  "org.scalatest" %%% "scalatest" % "3.0.8" % Test
)

libraryDependencies ++= Seq(
  "com.eed3si9n.verify" %%% "verify" % "0.1.0" % Test
)

testFrameworks += new TestFramework("verify.runner.Framework")

npmDependencies in Compile ++= Seq(
  "react" -> "16.8.6",
  "react-dom" -> "16.8.6",
  "react-proxy" -> "1.1.8",
  "file-loader" -> "3.0.1",
  "style-loader" -> "0.23.1",
  "css-loader" -> "2.1.1",
  "html-webpack-plugin" -> "3.2.0",
  "copy-webpack-plugin" -> "5.0.2",
  "webpack-merge" -> "4.2.1",
  "bootstrap" -> "4.3.1",
  "react-bootstrap" -> "1.0.0-beta.12",
  "libarchive.js" -> "1.2.0"
)

version in webpack := "4.29.6"
version in startWebpackDevServer := "3.2.1"

webpackResources := baseDirectory.value / "webpack" * "*"

webpackConfigFile in fastOptJS := Some(
  baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
)
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js")
webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js")

webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot")
webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()

requireJsDomEnv in Test := true

addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS")
addCommandAlias("build", "fullOptJS::webpack")
