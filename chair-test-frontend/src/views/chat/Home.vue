<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from "vue";
import { useRouter } from "vue-router";
import ChatFooter from "@/views/chat/footer/ChatFooter.vue";
import ChatMain from "@/views/chat/main/ChatMain.vue";
import ChatHeader from "@/views/chat/header/ChatHeader.vue";

const router = useRouter();

const show = ref(false);
const size = ref("medium");

// 点击页面空白关闭
const handleDocumentClick = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (!target.closest(".anchor-trigger")) {
    show.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
});
onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
});

const visible = ref(false);
</script>

<template>
  <chat-header />
  <chat-main />
  <ChatFooter />
</template>

<style scoped>
.chat-header {
  padding: 16px;
  background-color: #f7f7f7;
  border-bottom: 1px solid #ececec;
}

.user-info {
  float: right;
}

/* ========== CSS Anchor 核心 ========== */
.anchor-trigger {
  position: relative;
  display: inline-block;
}

.anchor-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: -16px;
  min-width: 120px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.menu-item {
  padding: 8px 14px;
  cursor: pointer;
  font-size: 14px;
  height: 25px;
  line-height: 25px;
  color: #333;
  border-radius: 8px;
}

.menu-item:hover {
  background-color: #f2f3f5;
}

/* =================================== */

.avatar {
  width: 40px;
  height: 40px;
  background-color: #007bff;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 12px;
}

.details h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.status {
  font-size: 12px;
}

.status.online {
  color: #28a745;
}

.status.offline {
  color: #dc3545;
}

.close-btn {
  margin-left: auto;
  color: #666;
  font-size: 12px;
}

.close-btn:hover {
  color: #ff4d4f;
}

.chat-window {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #e5ddd5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-window::-webkit-scrollbar {
  width: 6px;
}

.chat-window::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.message-row {
  display: flex;
  width: 100%;
}

.message-row.sent {
  justify-content: flex-end;
}

.message-row.received {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-row.sent .message-bubble {
  background-color: #dcf8c6;
  border-bottom-right-radius: 2px;
}

.message-row.received .message-bubble {
  background-color: #ffffff;
  border-bottom-left-radius: 2px;
}

.message-content {
  margin: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-time {
  display: block;
  font-size: 10px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.chat-input-area {
  text-align: center;
  padding: 12px;
  background-color: #f7f7f7;
  border-top: 1px solid #ececec;
}

.chat-input-area input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.2s;
}

.chat-input-area input:focus {
  border-color: #007bff;
}

.chat-input-area button {
  margin-left: 10px;
  padding: 0 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.chat-input-area button:hover:not(:disabled) {
  background-color: #0056b3;
}

.chat-input-area button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

.divider-demo {
  box-sizing: border-box;
}

.half-divider {
  min-width: auto;
  margin: 16px 0;
}

.flex-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-box .avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-right: 16px;
  color: var(--color-text-2);
  font-size: 16px;
  background-color: var(--color-fill-3);
  border-radius: 50%;
}

.flex-box .content {
  flex: 1;
  color: var(--color-text-2);
  font-size: 12px;
  line-height: 20px;
}

.arco-divider-horizontal {
  border-bottom: 2px solid rgb(142 144 143);
}
</style>
