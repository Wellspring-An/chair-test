package com.chair.chairdada.config;

import com.chair.chairdada.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@ConfigurationProperties(prefix = "chair")
@Data
public class TokenConfig {

    @Getter
    private String tokenName;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "chair";

    public static String setToken(String param) {
        return DigestUtils.md5DigestAsHex((SALT + param).getBytes());
    }

    public User getUserInfo() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        String token = request.getHeader(tokenName);

        if (token == null) {
            return null;
        }
        return (User) redisTemplate.opsForValue().get(token);
    }
}
