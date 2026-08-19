<script setup lang="ts">

import { useRouter } from "vue-router";

const router = useRouter();
const routerTo = (path: any) => {
  console.log(path);
  router.push(path);
}
</script>

<template>
  <!-- 输入区 -->
  <footer class="chat-input-area">
    <div class="grid-demo-background">
      <a-space direction="vertical" :size="16" style="display: block;">
        <a-row class="grid-demo">
          <a-col :span="12" @click="routerTo('/web/chat')" >
            <div>聊天</div>
          </a-col>
          <a-col :span="12" @click="routerTo('/web/chat/friends')" >
            <div>
              <icon-user-group />
              好友状态
            </div>
          </a-col>
        </a-row>
      </a-space>
    </div>
  </footer>
</template>

<style scoped>

</style>