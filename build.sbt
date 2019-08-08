enablePlugins(ScalaJSBundlerPlugin)

name := "epub-image-viewer"

scalaVersion := "2.13.0"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-P:scalajs:sjsDefinedByDefault"
)

npmDependencies in Compile += "react" -> "16.8.6"
npmDependencies in Compile += "react-dom" -> "16.8.6"
npmDependencies in Compile += "react-proxy" -> "1.1.8"

npmDevDependencies in Compile += "file-loader" -> "3.0.1"
npmDevDependencies in Compile += "style-loader" -> "0.23.1"
npmDevDependencies in Compile += "css-loader" -> "2.1.1"
npmDevDependencies in Compile += "html-webpack-plugin" -> "3.2.0"
npmDevDependencies in Compile += "copy-webpack-plugin" -> "5.0.2"
npmDevDependencies in Compile += "webpack-merge" -> "4.2.1"

libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.6.2"
libraryDependencies += "me.shadaj" %%% "slinky-hot" % "0.6.2"

libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.8" % Test

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
