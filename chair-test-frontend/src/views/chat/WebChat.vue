<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from "vue";
import { chatController } from "@/api/chatController";
import { useLoginUserStore } from "@/store/userStore";
import chatTypeEnum from "@/access/chatTypeEnum.ts";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const inputMessage = ref("");
const messages = reactive<API.ReceiveWebSocketMessage[]>([]);
// const isConnected = ref(false);
const chatWindowRef = ref<HTMLElement | null>(null);
const userStore = useLoginUserStore();

// 目标聊天好友ID，写死测试，实际切换好友赋值这个变量
const targetReceiverUserId = ref(route.params.id || 0);

const cleanups: (() => void)[] = [];

const formatTime = (date: Date): string => {
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight;
  }
};

const sendMessage = () => {
  const content = inputMessage.value.trim();
  if (!content) return;
  const currentUserId = userStore.loginUser?.id;
  if (!currentUserId) return;
  console.log(targetReceiverUserId.value);

  // 和后端ChatHandler.WebSocketMessage对齐
  const sentMsg: API.WebSocketMessage = {
    message: content,
    type: chatTypeEnum.USER_MESSAGE,
    sender: currentUserId,
    receiver: targetReceiverUserId.value,
    time: formatTime(new Date()),
  };
  const success = chatController.send(JSON.stringify(sentMsg));

  if (success) {
    // 本地渲染自己发出消息
    messages.push({
      ...sentMsg,
    });
    inputMessage.value = "";
    scrollToBottom();
  } else {
    alert("WebSocket 未连接，请稍后再试");
  }
};

onMounted(() => {
  cleanups.push(
    chatController.onMessage((raw: string) => {
      const parsedData = JSON.parse(raw) as API.ReceiveWebSocketMessage;

      // ✅关键过滤：好友申请通知不要渲染进聊天框
      if (parsedData.type === chatTypeEnum.ADD_USER_MESSAGE) {
        return;
      }
      // 普通聊天消息才加入列表
      messages.push(parsedData);
      scrollToBottom();
    }),
  );
});

onUnmounted(() => {
  cleanups.forEach((cleanup) => cleanup());
  // 组件销毁，绝不关闭全局ws
});

const routerTo = (path: any) => {
  console.log(path);
  router.push(path);
};
</script>

<template>
  <div class="chat-container">
    <div class="chat-card">
      <!-- 头部 -->
      <header class="chat-header">
        <div class="user-info">
          <div @click="routerTo('/web/chat')">
            <icon-to-left size="20" />
          </div>
          <div class="details">
            <h3>CodeGeeX 助手</h3>
            <!--            <span :class="['status', isConnected ? 'online' : 'offline']">-->
            <!--              {{ isConnected ? "在线" : "连接中..." }}-->
            <!--            </span>-->
          </div>
        </div>
      </header>

      <!-- 消息展示区 -->
      <main class="chat-window" ref="chatWindowRef">
        <div
          v-for="msg in messages"
          :key="msg.time + msg.message"
          :class="[
            'message-row',
            msg.sender === userStore.loginUser?.id ? 'sent' : 'received',
          ]"
        >
          <div class="message-bubble">
            <p class="message-content">{{ msg.message }}</p>
            <span class="message-time">{{ msg.time }}</span>
          </div>
        </div>
      </main>

      <!-- 输入区 -->
      <footer class="chat-input-area">
        <input
          type="text"
          v-model="inputMessage"
          placeholder="输入消息按回车发送..."
          @keyup.enter="sendMessage"
        />
        <button @click="sendMessage" :disabled="!inputMessage.trim()">
          发送
        </button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
/* 保持原有样式不变 */
.chat-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial,
    sans-serif;
}
.chat-card {
  width: 100%;
  max-width: 420px;
  height: 80vh;
  max-height: 700px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-header {
  padding: 16px;
  height: 20px;
  line-height: 20px;
  background-color: #f7f7f7;
  border-bottom: 1px solid #ececec;
}
.user-info {
  display: flex;
  align-items: center;
}
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
  position: relative;
  left: 25%;
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
  display: flex;
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
</style>
