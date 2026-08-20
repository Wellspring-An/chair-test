import request from "@/request";


/** check POST /api/ */
export async function updateUserAddFriend(
  body: API.UserFriendVo,
  options?: { [key: string]: any }
) {
  return request<any>("/api/userFriend/updateById", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body, // ✅ umi‑request post json 请求体用 params，不是 data
    ...(options || {}),
  });
}

/** check POST /api/ */
export async function addUserFriend(
  body: API.UserFriendVo,
  options?: { [key: string]: any }
) {
  return request<any>("/api/userFriend/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body, // ✅ umi‑request post json 请求体用 params，不是 data
    ...(options || {}),
  });
}

/** receiveMessage POST /api/ */
export async function selectAll(
  options?: { [key: string]: any }
) {
  return request<any>("/api/userFriend/selectAll", {
    method: "POST",
    ...(options || {}),
  });
}

/** receiveMessage POST /api/ */
export async function selectAddAll(
  options?: { [key: string]: any }
) {
  return request<any>("/api/userFriend/selectAddAll", {
    method: "POST",
    ...(options || {}),
  });
}
