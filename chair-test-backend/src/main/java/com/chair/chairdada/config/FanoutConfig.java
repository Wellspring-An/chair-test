package com.chair.chairdada.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutConfig {

    // fanout交换机名称，用于IM跨实例推送消息
    public static final String CHAT_FANOUT_EXCHANGE = "chat.fanout.exchange";

    @Bean
    public FanoutExchange chatFanoutExchange() {
        return ExchangeBuilder.fanoutExchange(CHAT_FANOUT_EXCHANGE).durable(true).build();
    }
}

