import { View } from "@tarojs/components";
import Taro from "@tarojs/taro";
import { AtButton, AtRadio } from "taro-ui";
import { window } from "@tarojs/runtime";
import { useEffect, useState } from "react";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";
import { generateUserAnswerIdUsingGet } from "../../api/userAnswerController";
import { listQuestionVoByPageUsingPost } from "../../api/questionController";

export default () => {
  interface Question {
    title: string;
    options: {
      result: string;
      score: number;
      value: string;
      key: string;
    }[];
  }

  // 当前题目序号从1开始
  const [current, setCurrent] = useState<number>(1);
  // 当前题目
  const [questions, setQuestions] = useState<Question[]>([]);
  const [currentQuestion, setCurrentQuestion] = useState<Question | null>(null);
  // 当前答案
  const [currentAnswer, setCurrentAnswer] = useState<string>("");
  // 回答列表
  const [answerList, setAnswerList] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const urlParams = new URLSearchParams(window.location.search);
  // appId
  const id = urlParams.get("id");

  // 获取问题数据
  const fetchQuestions = async () => {
    setIsLoading(true);
    try {
      const cachedQuestions = Taro.getStorageSync("questions");
      if (cachedQuestions) {
        setQuestions(cachedQuestions);
        return;
      }

      const res = await listQuestionVoByPageUsingPost({
        appId: id as any,
        current: 1,
        pageSize: 1,
        sortField: "createTime",
        sortOrder: "descend",
      });
      if (res?.data?.data?.records?.[0]?.questionContent) {
        const questionsData = res.data.data.records[0].questionContent;
        Taro.setStorageSync("questions", questionsData);
        setQuestions(questionsData);
      }
    } catch (error) {
      console.error("获取问题失败:", error);
      Taro.showToast({
        title: "获取问题失败",
        icon: "none",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getId = async () => {
    await generateUserAnswerIdUsingGet().then((res) => {
      Taro.setStorageSync("answerId", res.data.data);
    });
  };
  // 初始化问题和答案
  useEffect(() => {
    fetchQuestions();
    getId();
  }, []);

  // 序号变化时，切换题目和当前回答，当前问题变化时更新UI
  useEffect(() => {
    if (questions.length > 0 && current <= questions.length) {
      setCurrentQuestion(questions[current - 1]);
      setCurrentAnswer(answerList[current - 1] || "");
    }
  }, [current, questions, answerList]);

  // 处理选项选择
  const handleOptionSelect = (value: string) => {
    const newAnswerList = [...answerList];
    newAnswerList[current - 1] = value;
    setAnswerList(newAnswerList);
    setCurrentAnswer(value);

    // 自动进入下一题或提交
    if (current < questions.length) {
      setCurrent(current + 1);
    }
  };

  // 提交结果
  const submitAnswers = () => {
    if (answerList.length !== questions.length) {
      Taro.showToast({
        title: "请完成所有题目",
        icon: "none",
      });
      return;
    }

    Taro.setStorageSync("answerList", answerList);
    Taro.removeStorageSync("questions");
    Taro.navigateTo({
      url: "/pages/result/index?id=" + id,
    });
  };

  if (isLoading) {
    return <View className="loading">加载中...</View>;
  }

  if (!currentQuestion) {
    return <View className="error">暂无测试题目</View>;
  }

  const questionOptions = currentQuestion.options.map((option) => ({
    label: `${option.key}. ${option.value}`,
    value: option.key,
  }));

  const isLastQuestion = current === questions.length;
  const allAnswered = answerList.length === questions.length;

  return (
    <View className="doQuestionPage">
      {/*{JSON.stringify(answerList)}*/}
      <View className="at-article__h2 title">
        {current}：{currentQuestion.title}
      </View>

      <View className="options-wrapper">
        <AtRadio
          options={questionOptions}
          value={currentAnswer}
          onClick={handleOptionSelect}
        />
      </View>

      <View className="navigation-buttons">
        {current > 1 && (
          <AtButton
            circle
            className="controlBtn prev-btn"
            onClick={() => setCurrent(current - 1)}
          >
            上一题
          </AtButton>
        )}

        {!isLastQuestion && (
          <AtButton
            type="primary"
            circle
            className="controlBtn next-btn"
            onClick={() => setCurrent(current + 1)}
            disabled={!currentAnswer}
          >
            下一题
          </AtButton>
        )}

        {isLastQuestion && allAnswered && (
          <AtButton
            type="primary"
            circle
            className="controlBtn submit-btn"
            onClick={submitAnswers}
          >
            查看结果
          </AtButton>
        )}
      </View>

      <GlobalFooter />
    </View>
  );
};
