import { createApp } from "vue";
import App from "./App.vue";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import { createPinia } from "pinia";
import router from "./router";
import "@/access";
// 额外引入图标库
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import { chatController } from "@/api/chatController.ts";

// 先创建app实例
const app = createApp(App);

// 挂载全局
app.config.globalProperties.$chat = chatController;

app.use(ArcoVue).use(ArcoVueIcon).use(router).use(createPinia()).mount("#app");
