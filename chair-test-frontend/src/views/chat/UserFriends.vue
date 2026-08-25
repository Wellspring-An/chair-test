<script setup lang="ts">
import { onMounted, ref, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { selectAddAll, updateUserAddFriend } from "@/api/webChatController.ts";
import message from "@arco-design/web-vue/es/message";
import ChatFooter from "@/views/chat/footer/ChatFooter.vue";
import FriendStatusEnum from "@/access/friendStatusEnum.ts";

const myNotFriends = ref<API.UserFriendVo>([]);
const router = useRouter();

const contextMenuVisible = ref(false);
const menuX = ref(0);
const menuY = ref(0);
const currentOperateRecord = ref<API.UserFriendVo | null>(null);

const getMyNotFriends = async () => {
  const res = await selectAddAll();
  if (res.data.code === 0) {
    myNotFriends.value = res.data.data;
  } else {
    message.warning(res.data.message);
  }
};

onMounted(() => {
  getMyNotFriends();
  document.addEventListener("click", closeContextMenu);
});

onUnmounted(() => {
  document.removeEventListener("click", closeContextMenu);
});

const handleContextMenu = (e: MouseEvent, item: API.UserFriendVo) => {
  e.preventDefault();
  contextMenuVisible.value = true;
  menuX.value = e.clientX;
  menuY.value = e.clientY;
  currentOperateRecord.value = item;
};

const closeContextMenu = () => {
  contextMenuVisible.value = false;
  currentOperateRecord.value = null;
};

const agreeFriend = async (record: API.UserFriendVo) => {
  console.log("通过好友申请", record);
  closeContextMenu();
  updateUserAddFriend({ id: record.id, status: 1 }).then((res) => {
    if (res.data.code === 0) {
      message.success("已通过好友申请");
      getMyNotFriends();
    } else {
      message.warning(res.data.message);
    }
  });
};
const refuseFriend = async (record: API.UserFriendVo) => {
  console.log("拒绝好友", record);
  closeContextMenu();
  updateUserAddFriend({ id: record.id, status: 2 }).then((res) => {
    if (res.data.code === 0) {
      message.success("已拒绝好友申请");
      getMyNotFriends();
    } else {
      message.warning(res.data.message);
    }
  });
};
const deleteFriend = async (record: API.UserFriendVo) => {
  console.log("删除好友", record);
  closeContextMenu();
  updateUserAddFriend({ id: record.id, status: 3 }).then((res) => {
    if (res.data.code === 0) {
      message.success("已删除好友申请");
      getMyNotFriends();
    } else {
      message.warning(res.data.message);
    }
  });
};
const deFriend = async (record: API.UserFriendVo) => {
  console.log("拉黑好友", record);
  closeContextMenu();
  updateUserAddFriend({ id: record.id, status: 4 }).then((res) => {
    if (res.data.code === 0) {
      message.success("已拉黑好友申请");
      getMyNotFriends();
    } else {
      message.warning(res.data.message);
    }
  });
};
</script>

<template>
  <header class="chat-header"></header>
  <main class="chat-window">
    <div class="divider-demo">
      <a-collapse>
        <a-collapse-item v-for="item in myNotFriends" :key="item.id">
          <template #header>
            <!-- 标题行加tooltip，悬浮提示：右键进行操作 -->
            <a-tooltip content="右键进行操作" position="top">
              <div
                @contextmenu.prevent="handleContextMenu($event, item)"
                style="display: flex; align-items: center; gap: 80px"
              >
                <img
                  v-if="item.friendAvatar"
                  :src="item.friendAvatar"
                  style="
                    width: 32px;
                    height: 32px;
                    border-radius: 50%;
                    object-fit: cover;
                  "
                />
                <div
                  v-else
                  style="
                    width: 32px;
                    height: 32px;
                    border-radius: 50%;
                    background: #ccc;
                  "
                ></div>
                <span>{{ item.userAccount }}</span>
                <span style="margin-left: auto">{{
                  FriendStatusEnum[item.status]
                }}</span>
              </div>
            </a-tooltip>
          </template>

          <!-- 展开详情区域同样套tooltip，悬浮提示 -->
          <a-tooltip content="右键进行操作" position="top">
            <div @contextmenu.prevent="handleContextMenu($event, item)">
              <a-descriptions :column="6">
                <a-descriptions-item :span="1">账号：</a-descriptions-item>
                <a-descriptions-item :span="2">{{
                  item.userAccount
                }}</a-descriptions-item>
                <a-descriptions-item :span="1">昵称：</a-descriptions-item>
                <a-descriptions-item :span="2">{{
                  item.userName
                }}</a-descriptions-item>
              </a-descriptions>
              <a-descriptions :column="6">
                <a-descriptions-item :span="1">申请留言：</a-descriptions-item>
                <a-descriptions-item :span="5">{{
                  item.applyMsg
                }}</a-descriptions-item>
              </a-descriptions>
              <a-descriptions :column="6">
                <a-descriptions-item :span="1">申请时间：</a-descriptions-item>
                <a-descriptions-item :span="2">{{
                  item.applyTime
                }}</a-descriptions-item>
                <a-descriptions-item :span="1">通过时间：</a-descriptions-item>
                <a-descriptions-item :span="2">{{
                  item.agreeTime
                }}</a-descriptions-item>
              </a-descriptions>
            </div>
          </a-tooltip>
        </a-collapse-item>
      </a-collapse>

      <div
        v-if="contextMenuVisible && currentOperateRecord"
        class="native-context-menu"
        :style="{ left: `${menuX}px`, top: `${menuY}px` }"
        @click.stop
      >
        <div class="menu-item" @click="agreeFriend(currentOperateRecord!)">
          通过
        </div>
        <div class="menu-item" @click="refuseFriend(currentOperateRecord!)">
          拒绝
        </div>
        <div class="menu-item" @click="deleteFriend(currentOperateRecord!)">
          删除
        </div>
        <div class="menu-item" @click="deFriend(currentOperateRecord!)">
          拉黑
        </div>
      </div>
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

.divider-demo {
  box-sizing: border-box;
}

.native-context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  padding: 4px 0;
  min-width: 90px;
}
.menu-item {
  padding: 6px 16px;
  cursor: pointer;
  font-size: 14px;
}
.menu-item:hover {
  background-color: #f2f3f5;
}
</style>
