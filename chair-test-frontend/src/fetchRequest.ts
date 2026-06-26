import { Message } from "@arco-design/web-vue";

const BASE_URL = "http://localhost:8101";
const TIMEOUT = 60000;

// 请求拦截器
type RequestInterceptor = (
  config: RequestInit & { url: string }
) => RequestInit & { url: string };
// 响应拦截器
type ResponseInterceptor = (response: Response) => Response | Promise<Response>;

const requestInterceptors: RequestInterceptor[] = [];
const responseInterceptors: ResponseInterceptor[] = [];

/** 添加请求拦截器 */
export function addRequestInterceptor(interceptor: RequestInterceptor) {
  requestInterceptors.push(interceptor);
}

/** 添加响应拦截器 */
export function addResponseInterceptor(interceptor: ResponseInterceptor) {
  responseInterceptors.push(interceptor);
}

// 注册默认请求拦截器：自动携带 chair-token
addRequestInterceptor((config) => {
  const headers = new Headers(config.headers);
  const token = localStorage.getItem("chair-token");
  if (token) {
    headers.set("chair-token", token);
  }
  config.headers = headers;
  return config;
});

// 注册默认响应拦截器：处理 401 未登录
addResponseInterceptor((response) => {
  if (response.status === 40100 || response.status === 401) {
    const url = response.url;
    if (
      !url.includes("user/get/login") &&
      !window.location.pathname.includes("/user/login")
    ) {
      Message.warning("请先登录");
      window.location.href = `/user/login?redirect=${window.location.href}`;
    }
  }
  return response;
});

/**
 * 基于 fetch 的请求方法（支持流式响应）
 * 返回原始 Response 对象，调用方根据需要自行处理 .json() / .text() / .body 流
 */
export async function fetchRequest(
  url: string,
  options?: RequestInit & { timeout?: number }
): Promise<Response> {
  let config: RequestInit & { url: string } = {
    ...options,
    url: `${BASE_URL}${url}`,
    credentials: "include",
  };

  // 超时控制：若调用方未传入 signal，则用 AbortController + setTimeout 实现
  const timeout = options?.timeout ?? TIMEOUT;
  let timeoutId: ReturnType<typeof setTimeout> | undefined;
  if (timeout > 0 && !config.signal) {
    const timeoutController = new AbortController();
    config.signal = timeoutController.signal;
    timeoutId = setTimeout(() => timeoutController.abort(), timeout);
  }

  // 执行请求拦截器链
  for (const interceptor of requestInterceptors) {
    config = interceptor(config);
  }

  const { url: fullUrl, ...fetchOptions } = config;

  let response: Response;
  try {
    response = await fetch(fullUrl, fetchOptions);
  } finally {
    if (timeoutId !== undefined) clearTimeout(timeoutId);
  }

  // 执行响应拦截器链
  for (const interceptor of responseInterceptors) {
    response = await interceptor(response);
  }

  return response;
}

export default fetchRequest;
