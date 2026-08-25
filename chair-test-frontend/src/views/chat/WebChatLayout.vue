<script setup lang="ts"></script>

<template>
  <div class="chat-container">
    <div class="chat-card" id="chat-card">
      <router-view v-slot="{ Component }">
        <!-- 外层div：强制占满flex剩余高度，解决白屏/高度坍塌 -->
        <div class="router-wrap">
          <transition name="chat-slide">
            <component :is="Component" />
          </transition>
        </div>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
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
  position: relative;
  overflow: hidden;
}

/* 关键：撑满flex剩余空间，防止子组件高度0白屏 */
.router-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>

<style scoped>
:deep(.chat-slide-enter-from) {
  opacity: 0;
  transform: translateX(30px);
}
:deep(.chat-slide-leave-to) {
  opacity: 0;
  transform: translateX(-30px);
}
:deep(.chat-slide-enter-active),
:deep(.chat-slide-leave-active) {
  transition: all 0.28s ease;
}
</style>
