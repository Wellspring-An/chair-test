const userStore = {
  userInfo: {} as API.User,
  setUserInfo: setUserStore,
  isLogin: false
};

function setUserStore(user: any) {
  // TODO: 获取用户信息
  userStore.userInfo = user;
}

export default userStore;
