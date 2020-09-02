const path = require("path");

let config = {
  context: path.resolve(__dirname, "."),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    "jitsi-app": "./vue-app/main.js"
  },
  output: {
    filename: "js/[name].bundle.js"
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ["vue-style-loader", "css-loader"]
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: ["babel-loader", "eslint-loader"]
      },
      {
        test: /\.vue$/,
        use: ["vue-loader", "eslint-loader"]
      }
    ]
  }
};

module.exports = config;
