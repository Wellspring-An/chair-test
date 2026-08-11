//package com.chair.chairdada.controller;
//
//import com.alibaba.fastjson.JSON;
//import jakarta.websocket.*;
//import jakarta.websocket.server.ServerEndpoint;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@ServerEndpoint("/chat")
//public class ChatController {
//
//    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
//
//    // key:用户ID，value:用户会话session 线程安全HashMap
//    private static final Map<Long, Session> userSessionMap = new ConcurrentHashMap<>();
//
//    // 每个连接对应的当前登录用户id，存在session属性里
//    private static final String USER_ID_KEY = "userId";
//
//    /**
//     * 建立连接：url带上参数 ws://localhost:8080/chat?userId=1001
//     */
//    @OnOpen
//    public void onOpen(Session session) {
//        log.info("ChatController 新实例化，当前对象 HashCode: {}", this.hashCode());
//        // 解析url query参数 userId
//        String query = session.getQueryString();
//        Long userId;
//        try {
//            String[] arr = query.split("=");
//            userId = Long.parseLong(arr[1]);
//        } catch (Exception e) {
//            log.error("连接缺少userId参数，拒绝连接");
//            try {
//                session.close();
//            } catch (IOException ex) {
//            }
//            return;
//        }
//        // 把userId存入session属性
//        session.getUserProperties().put(USER_ID_KEY, userId);
//        userSessionMap.put(userId, session);
//
//        log.info("用户[{}]连接加入，在线用户数: {}", userId, userSessionMap.size());
//
//        // 广播群聊消息，通知所有人有人上线
//        broadcastAll(JSON.toJSONString(new ChatMsgDTO("system", null, null,
//                "系统消息：用户" + userId + "进入聊天室，在线人数:" + userSessionMap.size())));
//    }
//
//    @OnClose
//    public void onClose(Session session) {
//        Long userId = (Long) session.getUserProperties().get(USER_ID_KEY);
//        if (userId != null) {
//            userSessionMap.remove(userId);
//            log.info("用户[{}]断开连接，在线用户数:{}", userId, userSessionMap.size());
//            broadcastAll(JSON.toJSONString(new ChatMsgDTO("system", null, null,
//                    "系统消息：用户" + userId + "离开聊天室，在线人数:" + userSessionMap.size())));
//        }
//    }
//
//    /**
//     * 接收前端JSON消息
//     */
//    @OnMessage
//    public void onMessage(String jsonStr, Session session) {
//        Long senderId = (Long) session.getUserProperties().get(USER_ID_KEY);
//        log.info("收到用户[{}]消息:{}", senderId, jsonStr);
//
//        ChatMsgDTO dto;
//        try {
//            dto = JSON.parseObject(jsonStr, ChatMsgDTO.class);
//        } catch (Exception e) {
//            log.error("消息JSON解析失败", e);
//            return;
//        }
//
//        // 设置发送人id
//        dto.senderId = senderId;
//
//        if ("broadcast".equals(dto.type)) {
//            // ==========群聊广播，发给所有人==========
//            broadcastAll(JSON.toJSONString(dto));
//        } else if ("private".equals(dto.type)) {
//            // ==========私聊，点对点发给 receiverId==========
//            Long receiverId = dto.receiverId;
//            Session receiverSession = userSessionMap.get(receiverId);
//            if (receiverSession != null && receiverSession.isOpen()) {
//                sendOne(receiverSession, JSON.toJSONString(dto));
//            } else {
//                log.warn("私聊接收方{}不在线", receiverId);
//                // 【业务】这里可以保存离线消息到数据库
//            }
//            // 把消息回发给发送者自己，前端展示自己发出去的消息（可选）
//            sendOne(session, JSON.toJSONString(dto));
//        }
//    }
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        Long userId = (Long) session.getUserProperties().get(USER_ID_KEY);
//        log.error("WebSocket异常，userId={}", userId, error);
//    }
//
//    /**
//     * 发送给单个用户
//     */
//    private void sendOne(Session session, String msg) {
//        if (session == null || !session.isOpen()) return;
//        try {
//            session.getBasicRemote().sendText(msg);
//        } catch (IOException e) {
//            log.error("单发消息失败", e);
//        }
//    }
//
//    /**
//     * 广播给全部在线用户（群聊）
//     */
//    private void broadcastAll(String message) {
//        for (Session s : userSessionMap.values()) {
//            sendOne(s, message);
//        }
//    }
//
//    /**
//     * 消息DTO，前后端JSON协议
//     * type: broadcast群聊 / private私聊 / system系统消息
//     */
//    public static class ChatMsgDTO {
//        public String type;
//        public Long senderId;
//        public Long receiverId;
//        public String content;
//
//        public ChatMsgDTO() {
//        }
//
//        public ChatMsgDTO(String type, Long senderId, Long receiverId, String content) {
//            this.type = type;
//            this.senderId = senderId;
//            this.receiverId = receiverId;
//            this.content = content;
//        }
//    }
//}