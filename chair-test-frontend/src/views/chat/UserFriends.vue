<script setup lang="ts">
import { onMounted, reactive, ref, h } from "vue";
import { useRouter } from 'vue-router';
import { selectAddAll } from "@/api/webChatController.ts";
import message from "@arco-design/web-vue/es/message";
import ChatFooter from "@/views/chat/footer/ChatFooter.vue";
import FriendStatusEnum from "@/access/friendStatusEnum.ts";

const myNotFriends = ref<API.UserFriendVo>([]);
const router = useRouter();
const routerTo = (path: any) => {
  console.log(path);
  router.push(path);
}
const getMyNotFriends = async () => {
  const res = await selectAddAll();
  if (res.data.code === 0){
    myNotFriends.value = res.data.data;
  }else {
    message.warning(res.data.message);
  }
}
onMounted(() => {
  getMyNotFriends()
})

const agreeFriend = async (record: API.UserFriendVo) => {

}
const deleteFriend = async (record: API.UserFriendVo) => {

}
const deFriend = async (record: API.UserFriendVo) => {

}
</script>

<template>

  <header class="chat-header">

  </header>

  <!-- 消息展示区 -->
  <main class="chat-window">
    <div class="divider-demo">
      <a-collapse>
        <a-collapse-item v-for="item in myNotFriends" :key="item.id">
          <!-- collapse-item原生header插槽，不要挪位置 -->
          <template #header>
            <!-- 👉 这里div作为右键触发源 -->
            <a-dropdown trigger="contextMenu" alignPoint>
              <template #default>
                <div style="display:flex;align-items:center;gap:10px;">
                  <img
                    v-if="item.friendAvatar"
                    :src="item.friendAvatar"
                    style="width:32px;height:32px;border-radius:50%;object-fit:cover;"
                  />
                  <div v-else style="width:32px;height:32px;border-radius:50%;background:#ccc;"></div>
                  <span>{{ item.userAccount }}</span>
                  <span style="margin-left:auto">{{ FriendStatusEnum[item.status] }}</span>
                </div>
              </template>
              <template #content>
                <a-doption @click="agreeFriend(item)">通过</a-doption>
                <a-doption @click="deleteFriend(item)">删除</a-doption>
                <a-doption @click="deFriend(item)">拉黑</a-doption>
              </template>
            </a-dropdown>
          </template>

          <!-- collapse内容区域，不受dropdown影响 -->
          <a-descriptions :column="6">
            <a-descriptions-item :span="1">账号：</a-descriptions-item>
            <a-descriptions-item :span="2">{{ item.userAccount }}</a-descriptions-item>
            <a-descriptions-item :span="1">昵称：</a-descriptions-item>
            <a-descriptions-item :span="2">{{ item.userName }}</a-descriptions-item>
          </a-descriptions>
          <a-descriptions :column="6">
            <a-descriptions-item :span="1">申请留言：</a-descriptions-item>
            <a-descriptions-item :span="5">{{ item.applyMsg }}</a-descriptions-item>
          </a-descriptions>
          <a-descriptions :column="6">
            <a-descriptions-item :span="1">申请时间：</a-descriptions-item>
            <a-descriptions-item :span="2">{{ item.applyTime }}</a-descriptions-item>
            <a-descriptions-item :span="1">通过时间：</a-descriptions-item>
            <a-descriptions-item :span="2">{{ item.agreeTime }}</a-descriptions-item>
          </a-descriptions>
        </a-collapse-item>
      </a-collapse>
    </div>
  </main>
  <ChatFooter />
</template>

<style scoped>
.chat-header {
  padding: 16px;
  height: 20px;
  background-color: #f7f7f7;
  border-bottom: 1px solid #ececec;
}

/* =================================== */

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
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

.message-row.sent .message-bubble {
  background-color: #dcf8c6;
  border-bottom-right-radius: 2px;
}

.message-row.received .message-bubble {
  background-color: #ffffff;
  border-bottom-left-radius: 2px;
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