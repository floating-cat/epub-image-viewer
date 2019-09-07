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
  "dev.zio" %%% "zio" % "1.0.0-RC12-1",
  "dev.zio" %%% "zio-interop-cats" % "2.0.0.0-RC3",
  "org.typelevel" %%% "cats-core" % "2.0.0-RC2",
  "org.typelevel" %%% "cats-effect" % "2.0.0-RC2",
  "org.scalatest" %%% "scalatest" % "3.0.8" % Test
)

libraryDependencies ++= Seq(
  "com.eed3si9n.verify" %%% "verify" % "0.1.0" % Test
)

testFrameworks += new TestFramework("verify.runner.Framework")

npmDependencies in Compile ++= Seq(
  "react" -> "16.9.0",
  "react-dom" -> "16.9.0",
  "react-proxy" -> "1.1.8",
  "file-loader" -> "4.2.0",
  "style-loader" -> "1.0.0",
  "css-loader" -> "3.2.0",
  "html-webpack-plugin" -> "3.2.0",
  "copy-webpack-plugin" -> "5.0.4",
  "webpack-merge" -> "4.2.2",
  "bootstrap" -> "4.3.1",
  "libarchive.js" -> "1.3.0"
)

version in webpack := "4.39.3"
version in startWebpackDevServer := "3.8.0"

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
