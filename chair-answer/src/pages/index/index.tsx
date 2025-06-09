import { View, Image } from "@tarojs/components";
import { AtButton } from "taro-ui";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import { window } from "@tarojs/runtime";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";
import { getAppVoByIdUsingGet } from "../../api/appController";

export default () => {
  const [app, setApp] = useState([]); // 创建一个状态变量
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get("id");
    getAppVoByIdUsingGet({
      id: id,
    }).then((resApp) => {
      setApp(resApp.data.data);
    });
  }, []);
  return (
    <View className="indexPage">
      <View className="at-article__h1 title">{app.appName}</View>
      <View className="at-article__h2 subTitle">{app.appDesc}</View>
      <AtButton
        type="primary"
        className="enterBtn"
        circle
        onClick={() => {
          Taro.navigateTo({
            url: "/pages/doQuestion/index?id=" + app.id,
          });
        }}
      >
        开始测试
      </AtButton>
      <Image className="headerBg" src={app.appIcon} />
      <GlobalFooter />
    </View>
  );
};
