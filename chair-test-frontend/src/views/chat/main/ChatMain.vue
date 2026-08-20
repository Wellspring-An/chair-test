<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from "vue";
import { selectAll } from "@/api/webChatController.ts";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";

const router = useRouter();

const show = ref(false);
const size = ref("medium");
const myFriends = ref<API.UserFriendVo>([]);

// 点击页面空白关闭
const handleDocumentClick = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (!target.closest(".anchor-trigger")) {
    show.value = false;
  }
};

const getAllUser = async () => {
  const res = await selectAll();
  if (res.data.code === 0) {
    myFriends.value = res.data.data;
  }else {
    message.warning(res.data.message);
  }
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
  getAllUser();
});
onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
});

const routerTo = (path: any) => {
  console.log(path);
  router.push(path);
}
</script>

<template>
  <!-- 消息展示区 -->
  <main class="chat-window">
    <div class="divider-demo" v-for="item in myFriends">
      <div class="flex-box" @click="routerTo(`/web/chat/${item.friendId}`)">
        <span class="avatar"><a-image class="img-avatar" width="40px" height="40px" :src="item.friendAvatar" /></span>
        <div class="content">
          <a-typography-title :heading="6">{{item.userName}}</a-typography-title>
        </div>
      </div>
      <a-divider class="half-divider" />
    </div>
  </main>
</template>

<style scoped>
.flex-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-box .img-avatar {
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
</style>