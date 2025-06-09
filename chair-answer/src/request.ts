import Taro, { showToast } from "@tarojs/taro";

// async function request(option, method) {
//   const res = await Taro.request({
//     url: "https://lmwl2367139.vicp.fun"+option.url,
//     method: method,
//     data: option.data,
//     header: {
//       "content-type": "application/json",
//       'chair-token': Taro.getStorageSync('chair-token'),
//       ...option.header,
//     }
//   });
//   if (res.statusCode === 200) {
//     return res.data;
//   } else {
//     return Promise.reject(res);
//   }
// }
// export default request;

// 创建 Taro 请求实例
const request = async (url, config) => {
  // 添加全局请求配置
  const requestConfig = {
    ...config,
    url: `https://lmwl2367139.vicp.fun${url}`, // 添加基础路径
    timeout: 60000,
    credentials: "include", // 替代 withCredentials
    header: {
      ...config.header,
      "chair-token": Taro.getStorageSync("chair-token"), // Taro 的本地存储 API
    },
  };

  // 发送请求
  try {
    const response = await Taro.request(requestConfig);
    // 响应拦截处理
    const { data } = response;

    // 未登录处理 (40100)
    if (data.code === 40100) {
      const currentPages = Taro.getCurrentPages();
      const currentRoute = currentPages[currentPages.length - 1]?.route || "";

      // 避免登录循环
      if (
        !requestConfig.url.includes("user/get/login") &&
        !currentRoute.includes("user/login")
      ) {
        showToast({
          title: "请先登录",
          icon: "none",
        });

        // Taro 路由跳转
        Taro.redirectTo({
          url: `/pages/user/login?redirect=${encodeURIComponent(currentRoute)}`,
        });
      }
    }
    return response;
  } catch (error) {
    // 统一错误处理
    console.error("请求错误:", error);
    return await Promise.reject(error);
  }
};

export default request;
