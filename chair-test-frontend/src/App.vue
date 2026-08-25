<template>
  <div id="app">
    <template v-if="route.path.startsWith('/user')">
      <router-view />
    </template>
    <template v-else>
      <BasicLayout />
    </template>
  </div>
</template>

<style scoped></style>
<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";
import { useRoute } from "vue-router";
import { getCurrentInstance, onMounted, watch } from "vue";
import { useLoginUserStore } from "@/store/userStore.ts";

const instance = getCurrentInstance();
const $chat = instance?.appContext.config.globalProperties.$chat;
const userStore = useLoginUserStore();
const route = useRoute();

const doInit = () => {
  console.log("hello 欢迎来到我的项目");
  console.log(import.meta.env.VITE_API_BASE_URL);
};

onMounted(() => {
  doInit();
});

// ✅ 监听登录用户变化：登录 / 退出登录自动处理ws
watch(
  () => userStore.loginUser,
  (newUser) => {
    if (newUser?.id) {
      // 已登录，发起ws连接（chatController内部做防重复连接）
      $chat?.connect();
    } else {
      // 退出登录，关闭ws，禁止自动重连
      $chat?.close();
    }
  },
  { immediate: true }, // immediate:true，页面刚打开立刻执行一次
);
</script>
