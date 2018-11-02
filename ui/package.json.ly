{
  "name": "thingsboard",
  "private": true,
  "version": "2.1.0",
  "description": "Thingsboard UI",
  "licenses": [
    {
      "type": "Apache-2.0",
      "url": "http://www.apache.org/licenses/LICENSE-2.0"
    }
  ],
  "scripts": {
    "start": "babel-node --max_old_space_size=4096 server.js",
    "build": "cross-env NODE_ENV=production webpack -p",
    "build-dev": "webpack --config webpack.config.1.js -p"
  },
  "dependencies": {
    "@flowjs/ng-flow": "^2.7.1",
    "angular": "1.5.8",
    "angular-animate": "1.5.8",
    "angular-aria": "1.5.8",
    "angular-breadcrumb": "^0.4.1",
    "angular-carousel": "^1.0.1",
    "angular-cookies": "1.5.8",
    "angular-drag-and-drop-lists": "^1.4.0",
    "angular-fullscreen": "git://github.com/fabiobiondi/angular-fullscreen.git#master",
    "angular-gridster": "^0.13.14",
    "angular-hotkeys": "^1.7.0",
    "angular-jwt": "^0.1.6",
    "angular-material": "1.1.1",
    "angular-material-data-table": "^0.10.9",
    "angular-material-expansion-panel": "^0.7.2",
    "angular-material-icons": "^0.7.1",
    "angular-messages": "1.5.8",
    "angular-route": "1.5.8",
    "angular-sanitize": "1.5.8",
    "angular-socialshare": "^2.3.8",
    "angular-storage": "0.0.15",
    "angular-touch": "1.5.8",
    "angular-translate": "2.18.1",
    "angular-translate-handler-log": "2.18.1",
    "angular-translate-interpolation-messageformat": "2.18.1",
    "angular-translate-loader-static-files": "2.18.1",
    "angular-translate-storage-cookie": "2.18.1",
    "angular-translate-storage-local": "2.18.1",
    "angular-ui-ace": "^0.2.3",
    "angular-ui-router": "^0.3.1",
    "angular-websocket": "^2.0.1",
    "axios": "^0.18.0",
    "base64-js": "^1.2.1",
    "brace": "^0.10.0",
    "canvas-gauges": "^2.0.9",
    "clipboard": "^1.5.15",
    "compass-sass-mixins": "^0.12.7",
    "event-source-polyfill": "0.0.9",
    "flot": "git://github.com/thingsboard/flot.git#0.9-work",
    "flot.curvedlines": "^1.1.1",
    "font-awesome": "^4.6.3",
    "javascript-detect-element-resize": "^0.5.3",
    "jquery": "^3.3.1",
    "jquery.terminal": "^1.5.0",
    "js-beautify": "^1.6.4",
    "json-schema-defaults": "^0.2.0",
    "leaflet": "^1.0.3",
    "leaflet-providers": "^1.1.17",
    "material-ui": "^0.16.1",
    "material-ui-number-input": "^5.0.16",
    "md-color-picker": "^0.2.6",
    "mdPickers": "git://github.com/alenaksu/mdPickers.git#0.7.5",
    "moment": "^2.15.0",
    "mqtt": "^2.18.8",
    "ngFlowchart": "git://github.com/thingsboard/ngFlowchart.git#master",
    "ngclipboard": "^1.1.1",
    "ngreact": "^0.3.0",
    "objectpath": "^1.2.1",
    "oclazyload": "^1.0.9",
    "raphael": "^2.2.7",
    "rc-select": "^6.6.1",
    "react": "^15.4.1",
    "react-ace": "^4.1.0",
    "react-dom": "^15.4.1",
    "react-dropzone": "^3.7.3",
    "react-schema-form": "^0.3.1",
    "react-tap-event-plugin": "^2.0.1",
    "reactcss": "^1.0.9",
    "sass-material-colors": "0.0.5",
    "schema-inspector": "^1.6.6",
    "split.js": "^1.0.7",
    "tinycolor2": "^1.4.1",
    "tooltipster": "^4.2.4",
    "typeface-roboto": "0.0.22",
    "v-accordion": "^1.6.0"
  },
  "devDependencies": {
    "babel-cli": "^6.18.0",
    "babel-core": "^6.14.0",
    "babel-eslint": "^6.1.2",
    "babel-loader": "^6.2.5",
    "babel-preset-es2015": "^6.14.0",
    "babel-preset-react": "^6.16.0",
    "compression-webpack-plugin": "^1.1.11",
    "connect-history-api-fallback": "^1.3.0",
    "copy-webpack-plugin": "^3.0.1",
    "cross-env": "^3.2.4",
    "css-loader": "^0.25.0",
    "eslint": "^3.4.0",
    "eslint-config-angular": "^0.5.0",
    "eslint-loader": "^1.5.0",
    "eslint-plugin-angular": "^1.3.1",
    "eslint-plugin-import": "^1.14.0",
    "extract-text-webpack-plugin": "^1.0.1",
    "file-loader": "^0.9.0",
    "html-loader": "^0.4.3",
    "html-minifier": "^3.2.2",
    "html-minifier-loader": "^1.3.4",
    "html-webpack-plugin": "^2.30.1",
    "img-loader": "^1.3.1",
    "less": "^2.7.1",
    "less-loader": "^2.2.3",
    "ng-annotate-loader": "^0.1.1",
    "ngtemplate-loader": "^1.3.1",
    "node-sass": "^4.5.3",
    "postcss-loader": "^0.13.0",
    "raw-loader": "^0.5.1",
    "react-hot-loader": "^3.0.0-beta.6",
    "sass-loader": "^4.0.2",
    "style-loader": "^0.13.1",
    "url-loader": "^0.5.7",
    "webpack": "^1.13.2",
    "webpack-dev-middleware": "^1.6.1",
    "webpack-dev-server": "^1.15.1",
    "webpack-hot-middleware": "^2.12.2",
    "webpack-material-design-icons": "^0.1.0",
    "directory-tree": "^2.1.0",
    "jsonminify": "^0.4.1"
  },
  "engine": "node >= 5.9.0",
  "nyc": {
    "exclude": [
      "test",
      "__tests__",
      "node_modules",
      "target"
    ]
  }
}
