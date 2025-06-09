import { PropsWithChildren } from "react";
import Taro, { useLaunch } from "@tarojs/taro";
import "taro-ui/dist/style/index.scss"; // 引入组件样式 - 方式一
import "./app.scss";
import {
  userLoginByWxMiniOpenUsingGet,
  getUserVoByIdUsingGet
} from "./api/userController";

function App({ children }: PropsWithChildren) {
  const login = async () => {
    const res = await Taro.login();
    await userLoginByWxMiniOpenUsingGet({ code: res.code }).then((resUser) => {
      if (resUser.data.code === 0) {
        Taro.setStorageSync("chair-token", resUser.data.token);
      }
      // const userInfo = Taro.getUserProfile({
      //   desc: "用于完善会员资料",
      //   force: true,
      // }).then((res) => {
      //   updateUserInfo(res.userInfo);
      // });
      // console.log("获取用户信息：" + userInfo);
    });
  };
  // const updateUserInfo = async (userInfo) => {
  //   const res = await updateMyUserUsingPost({
  //     userName: userInfo.nickName,
  //     userAvatar: userInfo.avatarUrl,
  //   });
  //   if (res.data.code === 0) {
  //     console.log("更新用户信息成功");
  //   } else {
  //     console.log("更新用户信息失败" + res.data.code);
  //   }
  // };
  const getUserInfo = async (id) => {
    getUserVoByIdUsingGet({id: 1}).then((res) => {

    })
  }
  useLaunch(async () => {
    await login();
  });
  return children;
}

export default App;
