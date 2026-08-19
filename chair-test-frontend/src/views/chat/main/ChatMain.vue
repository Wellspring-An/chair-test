<script setup lang="ts">

</script>

<template>
  <!-- 消息展示区 -->
  <main class="chat-window">
    <div class="divider-demo" v-for="item in myFriends">
      <div class="flex-box" @click="routerTo(`/web/chat/${item.friendId}`)">
        <span class="avatar"><a-image :src="item.friendAvatar" /></span>
        <div class="content">
          <a-typography-title :heading="6">{{item.userName}}</a-typography-title>
          <!--        <a-typography-text>{{item.applyMsg}}</a-typography-text>-->
        </div>
      </div>
      <a-divider class="half-divider" />
    </div>
  </main>
</template>

<style scoped>

</style>