// Karma configuration file
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {},
      clearContext: false
    },
    jasmineHTML: {
      collapse: false
    },
    reporters: ['progress'],
    ports: {
      9876: true
    },
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['ChromeHeadless_NoSandbox'],
    browserDisconnectTimeout: 60000,
    browserNoActivityTimeout: 120000,
    processKillTimeout: 60000,
    restartOnFileChange: false,
    customLaunchers: {
      ChromeHeadless_NoSandbox: {
        base: 'ChromeHeadless',
        chromeOptions: {
          args: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage']
        }
      }
    },
    listenAddress: '0.0.0.0',
    hostname: '0.0.0.0'
  });
};
