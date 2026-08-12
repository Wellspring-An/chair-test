package com.chair.chairdada.aop;

import com.chair.chairdada.config.TokenConfig;
import com.chair.chairdada.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private TokenConfig tokenConfig;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession();
            String token = servletRequest.getServletRequest().getParameter("chair-token");

            // 校验token逻辑
            if (isValidToken(token)) {
                // 将用户信息存入attributes中，后续WebSocketHandler可以使用
                attributes.put("user", tokenConfig.getUserInfo(token));
                attributes.put("token", token);
                return true;
            } else {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后可以记录日志或执行清理操作
    }

    private boolean isValidToken(String token) {
        User userInfo = tokenConfig.getUserInfo(token);
        // 实际项目中应调用鉴权服务验证token
        return userInfo != null && !ObjectUtils.isEmpty(userInfo);
    }
}