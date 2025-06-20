import router from "@/router";
import { useLoginUserStore } from "@/store/userStore";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

//进入页面前，进行权限校验
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore();
  let loginUser = loginUserStore.loginUser;
  // 如果之前没有尝试获取过登录信息，才自动登录
  if (!loginUser || !loginUser.userRole) {
    // 加 await 是为了等待用户登录成功并且获取到值后，再执行后续操作
    await loginUserStore.fetchLoginUser();
    loginUser = loginUserStore.loginUser;
  }
  //当前页面需要的权限
  const needAccess = (to.meta.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    //如果没登录，跳转登录页面
    if (
      !loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESS_ENUM.NOT_LOGIN
    ) {
      next(`user/login?redirect=${to.fullPath}`);
    }
    //如果已经登录，判断权限是否足够，如果不足，跳转到无权限页面
    if (!checkAccess(loginUser, needAccess)) {
      next(`/noAuth?redirect=${to.fullPath}`);
      return;
    }
  }
  next();
});
