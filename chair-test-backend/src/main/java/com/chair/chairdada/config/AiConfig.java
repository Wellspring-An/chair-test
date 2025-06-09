package com.chair.chairdada.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    /**
     * 智谱AI
     */
    private String bigModelKey;

    @Bean
    public ClientV4 getClientV4() {
        return new ClientV4.Builder(bigModelKey).networkConfig(30,60,60,60, TimeUnit.SECONDS).build();
    }
}
