import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  console.log(">>>VITE_API_BASE_URL=", env.VITE_API_BASE_URL);
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    server: {
      port: 8008,
      proxy: {
        "/api": {
          target: env.VITE_API_BASE_URL, // .env.development => VITE_API_BASE_URL=http://localhost:8108
          changeOrigin: true,
          ws: true, // ⭐必须开启websocket代理
        },
      },
      open: false,
      cors: true,
    },
  };
});
