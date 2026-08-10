<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from "vue";
import { chatController } from "@/api/chatController";

interface Message {
  id: number;
  type: "sent" | "received";
  content: string;
  time: string;
}

const inputMessage = ref("");
const messages = reactive<Message[]>([]);
const isConnected = ref(false);
const chatWindowRef = ref<HTMLElement | null>(null);

let messageIdCounter = 0;
let streamingMsgId: number | null = null;
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

  const success = chatController.send(content);

  if (success) {
    const sentMsg: Message = {
      id: ++messageIdCounter,
      type: "sent",
      content: content,
      time: formatTime(new Date()),
    };
    messages.push(sentMsg);
    streamingMsgId = null;
    inputMessage.value = "";
    scrollToBottom();
  } else {
    alert("WebSocket 未连接，请稍后再试");
  }
};

const closeConnection = () => {
  if (isConnected.value === true) {
    chatController.close();
    isConnected.value = false;
  }else {
    chatController.connect();
    isConnected.value = true;
  }
};

onMounted(() => {
  // ✅关键：组件挂载瞬间读取全局controller真实状态！解决切页面状态不同步
  isConnected.value = chatController.isOpen;

  // 注册事件监听
  cleanups.push(
    chatController.onOpen(() => {
      isConnected.value = true;
    })
  );

  cleanups.push(
    chatController.onClose(() => {
      isConnected.value = false;
    })
  );

  cleanups.push(
    chatController.onMessage((raw: string) => {
      const chunk = raw;
      if (streamingMsgId === null) {
        const newMsg: Message = {
          id: ++messageIdCounter,
          type: "received",
          content: chunk,
          time: formatTime(new Date()),
        };
        messages.push(newMsg);
        streamingMsgId = newMsg.id;
      } else {
        const target = messages.find((m) => m.id === streamingMsgId);
        if (target) {
          target.content += chunk;
        }
      }
      scrollToBottom();
    })
  );

  chatController.connect(); // 只保留一次
});

onUnmounted(() => {
  cleanups.forEach((cleanup) => cleanup());
  // chatController.close(); // 全局单例，组件销毁不要关闭ws！注释保持
});
</script>

<template>
  <div class="chat-container">
    <div class="chat-card">
      <!-- 头部 -->
      <header class="chat-header">
        <div class="user-info">
          <div class="avatar">CG</div>
          <div class="details">
            <h3>CodeGeeX 助手</h3>
            <span :class="['status', isConnected ? 'online' : 'offline']">
              {{ isConnected ? "在线" : "连接中..." }}
            </span>
          </div>
          <a-button
            type="text"
            class="close-btn"
            @click="closeConnection"
          >
            {{isConnected ? '关闭连接' : '打开连接'}}
          </a-button>
        </div>
      </header>

      <!-- 消息展示区 -->
      <main class="chat-window" ref="chatWindowRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-row', msg.type === 'sent' ? 'sent' : 'received']"
        >
          <div class="message-bubble">
            <p class="message-content">{{ msg.content }}</p>
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
          :disabled="!isConnected"
        />
        <button
          @click="sendMessage"
          :disabled="!isConnected || !inputMessage.trim()"
        >
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
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
  Arial, sans-serif;
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
