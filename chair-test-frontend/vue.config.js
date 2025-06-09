const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8008,
    allowedHosts: ["2367v139x2.zicp.fun"],
  },
});
