package com.chair.chairdada.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

import java.util.Date;

@Configuration
@ConfigurationProperties(prefix = "chair")
@Data
public class TokenConfig {

    private String tokenName;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "chair";

    public String getTokenName() {
        return tokenName;
    }

    public static String setToken(String param) {
        return DigestUtils.md5DigestAsHex((SALT + param).getBytes());
    }
}
