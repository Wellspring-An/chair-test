<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from "vue";
import { listUserAllVO } from "@/api/userController.ts";
import { addUserFriend, selectAll } from "@/api/webChatController.ts";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";

const router = useRouter();

const show = ref(false);
const size = ref("medium");
const users = ref<API.User>([]);
const myFriends = ref<API.UserFriendVo>([]);

// 点击页面空白关闭
const handleDocumentClick = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (!target.closest(".anchor-trigger")) {
    show.value = false;
  }
};

const userFriend = reactive<API.UserFriendVo>({});

const searchUser = async () => {
  const res = await listUserAllVO();
  if (res.data.code === 0 && res.data.data) {
    users.value = res.data.data;
  }
};

const addFriend = async () => {
  const res = await addUserFriend(userFriend);
  if (res.data.code !== 0) {
    message.warning(res.data.message);
  }
}

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

const visible = ref(false);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
  addFriend();
};
const handleCancel = () => {
  visible.value = false;
};
const routerTo = (path: any) => {
  console.log(path);
  router.push(path);
}
</script>

<template>
<header class="chat-header">
  <div class="user-info">
    <div class="details">
      <div class="anchor-trigger" @click.stop="show = !show">
        <icon-plus v-if="!show" style="cursor: pointer; font-size:18px;" />
        <icon-close v-if="show" style="cursor: pointer; font-size:18px;" />

        <!-- 原生锚点菜单 -->
        <div v-if="show" class="anchor-menu">
          <div class="menu-item" @click="handleClick">添加好友</div>
        </div>
      </div>
    </div>
  </div>
</header>

<!-- 消息展示区 -->
<main class="chat-window">
  <div class="divider-demo" v-for="item in myFriends">
    <div class="flex-box" @click="routerTo(`/web/chat/${item.friendId}`)">
      <span class="avatar"><a-image :src="item.friendAvatar" /></span>
      <div class="content">
        <a-typography-title :heading="6">{{item.friendId}}</a-typography-title>
<!--        <a-typography-text>{{item.applyMsg}}</a-typography-text>-->
      </div>
    </div>
    <a-divider class="half-divider" />
  </div>
</main>

<!-- 输入区 -->
<footer class="chat-input-area">
  <div class="grid-demo-background">
    <a-space direction="vertical" :size="16" style="display: block;">
      <a-row class="grid-demo">
        <a-col :span="12">
          <div>首页</div>
        </a-col>
        <a-col :span="12">
          <div @click="routerTo('/web/chat/friends')">
            <icon-user-group />
            好友
          </div>
        </a-col>
      </a-row>
    </a-space>
  </div>
</footer>
<a-drawer
  popup-container="#chat-card"
  :visible="visible"
  placement="top"
  @ok="handleOk"
  @cancel="handleCancel"
>
  <template #title> 添加好友</template>
  <div
  >
    <a-space direction="vertical" size="large">
      <a-select v-model="userFriend.friendId" :style="{width:'320px'}" :size="size"
                @search="searchUser"
                placeholder="请输入登录名或名字"
                allow-search
      >
        <a-option v-for="item of users" :value="item.id">
          <a-image width="25" height="25" :src="item.userAvatar" />
          <span style="padding-left: 5px">{{ item.userAccount }} - {{ item.userName }}</span></a-option>
      </a-select>
      <a-input v-model="userFriend.applyMsg" placeholder="请输入申请原因" />
    </a-space>
  </div
  >
</a-drawer>
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