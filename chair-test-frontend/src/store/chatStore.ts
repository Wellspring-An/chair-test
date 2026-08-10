// src/store/chatStore.ts
import { ref } from 'vue';
import { chatController } from '@/api/chatController';

export const useChatStore = () => {
  const isConnected = ref(false);

  // 初始化连接状态
  chatController.onOpen(() => {
    isConnected.value = true;
  });

  chatController.onClose(() => {
    isConnected.value = false;
  });

  return {
    isConnected
  };
};
