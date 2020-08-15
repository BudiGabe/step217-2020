module.exports = {
    extends: 'lighthouse:default',
    passes: [
      { passName: 'defaultPass', pauseAfterLoadMs: 45000 }
    ],
    ci: {
      collect: {
        url: ['http://localhost:8080/'],
        startServerCommand: 'mvn package appengine:run',
      },
      upload: {
        target: 'temporary-public-storage'
      },
    },
};
