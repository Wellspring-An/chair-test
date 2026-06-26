// @ts-ignore;
/* eslint-disable */
import request from "@/request";
import fetchRequest from "@/fetchRequest";

/** addApp POST /api/app/add */
export async function addAppUsingPost(
  body: API.AppAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>("/api/app/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteApp POST /api/app/delete */
export async function deleteAppUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/app/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** editApp POST /api/app/edit */
export async function editAppUsingPost(
  body: API.AppEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/app/edit", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** getAppVOById GET /api/app/get/vo */
export async function getAppVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO_>("/api/app/get/vo", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listAppByPage POST /api/app/list/page */
export async function listAppByPageUsingPost(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageApp_>("/api/app/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** listAppVOByPage POST /api/app/list/page/vo */
export async function listAppVoByPageUsingPost(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO_>("/api/app/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** listMyAppVOByPage POST /api/app/my/list/page/vo */
export async function listMyAppVoByPageUsingPost(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO_>("/api/app/my/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** doAppReview POST /api/app/review */
export async function doAppReviewUsingPost(
  body: API.ReviewRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/app/review", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** updateApp POST /api/app/update */
export async function updateAppUsingPost(
  body: API.AppUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/app/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}


/** aiChat POST /api/Ai/test */
export async function aiChat(
  body: API.AiRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/Ai/test", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    data: body,
    ...(options || {}),
  });
}

/** aiChat POST /api/Ai/test */
export async function aiBot(
  body: API.AiBotRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/Ai/testAddApp", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    data: body,
    ...(options || {}),
  });
}

/** aiChatStream POST /api/Ai/testStream（返回原始 Response，用于流式读取） */
export async function aiChatStream(
  body: API.AiRequest,
  options?: { [key: string]: any }
) {
  return fetchRequest("/api/Ai/testStream", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      Accept: "text/event-stream",
      "Cache-Control": "no-cache",
    },
    body: new URLSearchParams(body as Record<string, string>),
    ...(options || {}),
  });
}

/** aiChatStream POST /api/Ai/testStream（返回原始 Response，用于流式读取） */
export async function aiBotStream(
  body: API.AiBotRequest,
  options?: { [key: string]: any }
) {
  return fetchRequest("/api/Ai/testAddAppStream", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      Accept: "text/event-stream",
      "Cache-Control": "no-cache",
    },
    body: new URLSearchParams(body as Record<string, string>),
    ...(options || {}),
  });
}