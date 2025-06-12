package com.chair.chairdada.bigmodel;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.chair.chairdada.common.ErrorCode;
import com.chair.chairdada.exception.ThrowUtils;
import org.elasticsearch.core.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class DeepSeekUtil {
    private static final String API_URL = "http://localhost:11434";

    public static final String DeepSeekR1_8b = "deepseek-r1:8b";
    public static final String DeepSeekR1_7b = "deepseek-r1:7b";
    public static final String DeepSeekR1_1b5b = "deepseek-r1:1.5b";
    private static final List<String> modelList = new ArrayList<>();

    @Bean
    public void initModels() {
        modelList.add(DeepSeekR1_8b);
        modelList.add(DeepSeekR1_7b);
        modelList.add(DeepSeekR1_1b5b);
    }

    public static String askDeepSeek(String prompt, String model) throws IOException {
        JSONObject param = new JSONObject();

        for (String s : modelList) {
            if (s.equals(model)) {
                param.set("model", s)  // 指定模型
                        .set("prompt", prompt)
                        .set("stream", false)              // 关闭流式响应
                        .set("temperature", 0.7);          // 控制生成随机性
            } else {
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "模型不存在");
            }
        }

        String response = HttpRequest.post(API_URL + "/api/generate")
                .body(param.toString())
                .timeout(3000000)                // 3000秒超时
                .execute()
                .body();

        // 解析响应（示例：FastJSON2）
        return new JSONObject(response).getStr("response");
    }

    public Flux<String> askDeepSeekSteam(String prompt) {
        WebClient client = WebClient.create("http://localhost:11434");

        return client.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON) // 必须设置JSON类型
                .bodyValue(Map.of(
                        "model", "deepseek-r1:7b",
                        "prompt", prompt,
                        "stream", true,
                        "temperature", 0.7
//                        "message", "Hello, world!"
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.contains("\"done\":true")) // 过滤结束标记
                .map(data -> new JSONObject(data).get("response").toString());
    }
}