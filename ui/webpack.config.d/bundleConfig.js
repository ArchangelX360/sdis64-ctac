const HtmlWebpackPlugin = require('html-webpack-plugin');

config.output = {
    ...config.output,
    filename: "[name].[contenthash].js",
}

config.optimization = {
    ...config.optimization,
    moduleIds: 'deterministic',
    runtimeChunk: 'single',
    splitChunks: {
        cacheGroups: {
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendors',
                chunks: 'all',
            },
            libs: {
                // TODO: it would be nice to infer `ctac-ui` name from somewhere
                test: /kotlin[\\/](?!ctac-ui)/,
                name: 'kotlin-vendors',
                chunks: 'all',
            },
        },
    },
}

const htmlPlugin = new HtmlWebpackPlugin({
    template: 'kotlin/index.html',
    publicPath: '/',
})
config.plugins.push(htmlPlugin)
