import { createRouter, createWebHistory } from "vue-router";
import { routes } from "@/router/routes";
import { chatController } from "@/api/chatController.ts";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to, from, next) => {
  // 如果是聊天页面，确保连接
  if (to.path.startsWith("web/chat")) {
    chatController.connect();
  }
  next();
});

export default router;
