import { Image, View } from "@tarojs/components";
import Taro from "@tarojs/taro";
import { AtCard } from "taro-ui";
import { useEffect, useState } from "react";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";
import { listAppVoByPageUsingPost } from "../../api/appController";

export default () => {
  const [dataList, setDataList] = useState([]); // 创建一个状态变量
  useEffect(() => {
    listAppVoByPageUsingPost({
      reviewStatus: 1,
      current: 1,
      pageSize: 12,
      sortOrder: "descend",
      sortField: "createTime",
    }).then((resApps) => {
      setDataList(resApps.data.data.records);
    });
  }, []);
  return (
    <View className="homePage">
      <View className="at-article__h1 title">源 - 测试</View>
      <View className="at-article__h2 subTitle">能非常准确的测试你的能力</View>
      {dataList.map((item: any) => (
        <AtCard
          className="listItem"
          title={item.appName}
          onClick={() => {
            Taro.reLaunch({
              url: "/pages/index/index?id=" + item.id,
            });
          }}
        >
          <View className="itemImage">
            {item.appDesc}
            <Image src={item.appIcon} />
          </View>
        </AtCard>
      ))}

      <GlobalFooter />
    </View>
  );
};
