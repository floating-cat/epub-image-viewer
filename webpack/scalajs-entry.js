if (process.env.NODE_ENV === "production") {
    const opt = require("./epub-image-viewer-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./epub-image-viewer-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./epub-image-viewer-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
