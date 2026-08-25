<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="titleBar">
            <img class="logo" src="@/assets/logo.png" alt="" />
            <div class="title">源 - 测试</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="route in visibleRoutes" :key="route.path">
          {{ route.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <div v-if="loginUserStore.loginUser.id">
        <a-space>
          <a-dropdown position="bottom">
            <div>
              <a-avatar
                :size="24"
                :image-url="loginUserStore.loginUser?.userAvatar"
                :style="{ marginRight: '8px' }"
              />
              {{ loginUserStore.loginUser.userName ?? "无名" }}
            </div>
            <template #content>
              <a-doption @click="logout">退出</a-doption>
            </template>
          </a-dropdown>
        </a-space>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
      <a-modal
        v-model:visible="visible"
        @ok="handleOk"
        @cancel="handleCancel"
        draggable
      >
        <template #title> 是否退出登录 </template>
        <div>退出登录，您将无法使用现有功能！！！</div>
      </a-modal>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useLoginUserStore } from "@/store/userStore";
import checkAccess from "@/access/checkAccess";

const loginUserStore = useLoginUserStore();

const router = useRouter();
//当前选中菜单项
const selectedKeys = ref(["/"]);
// 点击确定按钮
const visible = ref(false);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
  useLoginUserStore().logoutUser();
  loginUserStore.loginUser = {} as any;
};
const handleCancel = () => {
  visible.value = false;
};
//路由跳转时，自动更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});

//展示在菜单栏的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((route) => {
    if (route.meta?.hiddenInMenu) {
      return false;
    }
    //根据权限过滤菜单
    if (!checkAccess(loginUserStore.loginUser, route.meta?.access as string)) {
      return false;
    }
    return true;
  });
});
//点击菜单跳转路由
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
// 退出登录
const logout = () => {
  visible.value = true;
};
</script>

<style scoped>
#globalHeader {
}

.titleBar {
  display: flex;
  align-items: center;
}

title {
  color: black;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
