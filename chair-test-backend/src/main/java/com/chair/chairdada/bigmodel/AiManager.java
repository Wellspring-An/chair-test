package com.chair.chairdada.bigmodel;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.chair.chairdada.common.ErrorCode;
import com.chair.chairdada.exception.ThrowUtils;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import okhttp3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Configuration
public class AiManager {
    private static final String API_URL = "http://localhost:11434";

    public static final String DeepSeekR1_8b = "deepseek-r1:8b";
    public static final String DeepSeekR1_7b = "deepseek-r1:7b";
    public static final String DeepSeekR1_1b5b = "deepseek-r1:1.5b";
    private static final List<String> modelList = new ArrayList<>();

    // 稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    //不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;


    @Bean
    public void initModels() {
        modelList.add(DeepSeekR1_8b);
        modelList.add(DeepSeekR1_7b);
        modelList.add(DeepSeekR1_1b5b);
    }

    public String askDeepSeek(String userMessage,String sysMessage,String model) {
        List<AiMessage> messages = List.of(
                new AiMessage("system", sysMessage),
                new AiMessage("user", userMessage)
        );
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.set("messages",messages);
        JSONObject jsonObject = new JSONObject();

        for (String s : modelList) {
            if (s.equals(model)) {
                jsonObject.set("model", s);         // 指定模型
                jsonObject.set("stream", false);    // 关闭流式响应
                jsonObject.set("temperature", 0.7); // 控制生成随机性
                jsonObject.set("prompt", jsonMessages.toString());
            } else {
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "模型不存在");
            }
        }

        String response = HttpRequest.post(API_URL + "/api/generate")
                .body(jsonObject.toString())
                .timeout(3000000)                // 3000秒超时
                .execute()
                .body();

        // 解析响应（示例：FastJSON2）
        return new JSONObject(response).getStr("response");
    }

    public Flux<String> askDeepSeekSteam(String userMessage,String sysMessage,String model) {
        WebClient client = WebClient.create(API_URL);
        List<AiMessage> messages = List.of(
                new AiMessage("system", sysMessage),
                new AiMessage("user", userMessage)
        );
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("messages",messages);


        return client.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON) // 必须设置JSON类型
                .bodyValue(Map.of(
                        "model", model,
                        "prompt", jsonObject.toString(),
                        "stream", true,
                        "temperature", 0.7
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.contains("\"done\":true")) // 过滤结束标记
                .map(data -> new JSONObject(data).get("response").toString());
    }

    public Flowable<String> askDeepSeekSteamRx(String userMessage, String sysMessage, String model, boolean isThink) {
        // 1. 构建请求体（与原逻辑一致）
        List<AiMessage> messages = List.of(
                new AiMessage("system", sysMessage),
                new AiMessage("user", userMessage)
        );
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messages", messages);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", jsonObject.toString(),
                "stream", true,
                "temperature", 0.7
        );
        if (!isThink){
            requestBody.put("think",false);
        }

        // 2. 创建 OkHttpClient（RxJava 兼容的 HTTP 客户端）
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // 3. 构建请求
        Request request = new Request.Builder()
                .url(API_URL + "/api/generate")
                .post(RequestBody.create(
                        new JSONObject(requestBody).toString().getBytes()
                ))
                .build();

        // 4. 返回 Flowable 流
        return Flowable.create(emitter -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    emitter.onError(new RuntimeException("HTTP error: " + response.code()));
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    emitter.onComplete();
                    return;
                }

                // 5. 流式读取响应
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(body.byteStream())
                )) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 6. 过滤 done:true 标记（与原逻辑一致）
                        if (line.contains("\"done\":true")) continue;

                        // 7. 提取 response 字段（与原逻辑一致）
                        JSONObject json = new JSONObject(line);
                        String chunk = json.getStr("response");
                        emitter.onNext(chunk);
                    }
                    emitter.onComplete();
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER); // 背压策略
    }
}