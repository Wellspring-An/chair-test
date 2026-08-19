<script setup lang="ts">
</script>

<template>
  <div class="chat-container">
    <div class="chat-card" id="chat-card">
      <router-view></router-view>
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
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
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
</style>