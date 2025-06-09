import { View } from "@tarojs/components";
import Taro from "@tarojs/taro";
import { window } from "@tarojs/runtime";
import { useEffect, useState } from "react";
import { AtButton } from "taro-ui";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";
import {
  addUserAnswerUsingPost,
  getUserAnswerVoByIdUsingGet,
} from "../../api/userAnswerController";

export default () => {
  interface Answer {
    resultName: string;
    resultDesc: string;
    resultScore?: number;
    choices: string;
    appId: number;
    appType: number;
    scoringStrategy: number;
  }

  //获取答案
  const answerList = Taro.getStorageSync("answerList");
  const [isShowResult, setIsLoading] = useState<boolean>(false);
  const [answers, setAnswers] = useState<Answer>();

  const doAnswer = async () => {
    // 从 url 获取值
    const urlParams = new URLSearchParams(window.location.search);
    // appId
    const appId = urlParams.get("id");
    // answerId
    const id = Taro.getStorageSync("answerId");
    await addUserAnswerUsingPost({
      appId: appId,
      choices: answerList,
      id: id,
    }).then((res) => {
      if (res.data.code === 0) {
        if (res?.data?.data) getAnswer(res.data.data);
      }
    });
  };
  useEffect(() => {
    doAnswer();
  }, []);

  const getAnswer = async (answerId) => {
    await getUserAnswerVoByIdUsingGet({
      id: answerId,
    }).then((res) => {
      if (res.data.code === 0) {
        if (res?.data?.data) {
          setIsLoading(true);
          setAnswers(res.data.data);
        }
      }
    });
  };
  if (!isShowResult) {
    return <View className="loading">正在评分中...</View>;
  }
  return (
    <View className="indexPage">
      <View className="text-contain">
        <View className="at-article__h1 title">{answers.resultName}</View>
        <View className="at-article__h2 subTitle">{answers.resultDesc}</View>
      </View>
      <AtButton
        type="primary"
        className="enterBtn"
        circle
        onClick={() => {
          Taro.reLaunch({
            url: "/pages/home/index",
          });
        }}
      >
        返回主页
      </AtButton>
      {/*<Image className="headerBg" src="http://img1.baidu.com/it/u=620864010,3118089474&fm=253&app=138&f=JPEG?w=800&h=1422" />*/}
      <GlobalFooter />
    </View>
  );
};
