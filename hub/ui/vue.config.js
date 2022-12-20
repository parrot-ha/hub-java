module.exports = {
  transpileDependencies: ['vuetify'],
  devServer: {
    proxy: {
      '^/api': {
        target: 'http://localhost:7000',
        changeOrigin: true
      }
    }
  }
};
