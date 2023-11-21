//webpack.config.d/additional-config.js
const webpack = require("webpack");

function getCtacApiUrl() {
    switch (config.mode) {
        case "production":
            return "https://api-ctac.sdis64.fr/backend"
        case "development":
            // return "http://localhost:8081"
            return "https://staging-api-ctac.sdis64.fr/backend"
        default:
            return "https://staging-api-ctac.sdis64.fr/backend"
    }
}

const definePlugin = new webpack.DefinePlugin({
    // https://github.com/webpack/webpack/issues/8641
    CTAC_API_URL: JSON.stringify(getCtacApiUrl()),
})

config.plugins.push(definePlugin)
