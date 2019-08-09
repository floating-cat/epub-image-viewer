var path = require("path");
var CopyWebpackPlugin = require('copy-webpack-plugin');
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  mode: "development",
  resolve: {
    alias: {
      "resources": path.resolve(__dirname, "../../../../src/main/resources"),
      "js": path.resolve(__dirname, "../../../../src/main/js"),
      "scalajs": path.resolve(__dirname, "./scalajs-entry.js")
    },
    modules: [ path.resolve(__dirname, 'node_modules') ]
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [ 'style-loader', 'css-loader' ]
      },
      // "file" loader for svg
      {
        test: /\.svg$/,
        use: [
          {
            loader: 'file-loader',
            query: {
              name: 'static/media/[name].[hash:8].[ext]'
            }
          }
        ]
      }
    ]
  },
  plugins: [
    // Web Worker need a js file to do work
    // so we export this js file separately
    new CopyWebpackPlugin([
      { from: "libarchive.js/dist", context: 'node_modules' }
    ]),
    new CopyWebpackPlugin([
      { from: path.resolve(__dirname, "../../../../public") }
    ]),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, "../../../../public/index.html")
    })
  ],
  output: {
    devtoolModuleFilenameTemplate: (f) => {
      if (f.resourcePath.startsWith("http://") ||
          f.resourcePath.startsWith("https://") ||
          f.resourcePath.startsWith("file://")) {
        return f.resourcePath;
      } else {
        return "webpack://" + f.namespace + "/" + f.resourcePath;
      }
    }
  }
}
