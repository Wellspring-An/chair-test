import { getLoginUserUsingGet, userLogoutUsingPost } from "@/api/userController";
import { defineStore } from "pinia";
import { ref } from "vue";
import ACCESS_ENUM from "@/access/accessEnum";
import { useRouter } from "vue-router";

/**
 * 登录用户信息全局状态
 */
export const useLoginUserStore = defineStore("loginUser", () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: "未登录",
  });

  const router = useRouter();

  const token = ref<string>();

  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser;
  }

  async function logoutUser() {
    const res = await userLogoutUsingPost();
    if (res.data.code === 0 && res.data.data) {
      if (res.data.data === true) {
        localStorage.removeItem("chair-token")
        router.push("/")
      }else {
        confirm("退出失败，请重试")
        router.push("/")
      }
    }else {
      confirm("退出失败，请重试")
      router.push("/")
    }
  }

  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet();
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data;
    } else {
      loginUser.value = { userRole: ACCESS_ENUM.NOT_LOGIN };
    }
  }

  return { loginUser, setLoginUser, logoutUser, token, fetchLoginUser };
});
