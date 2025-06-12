package com.chair.chairdada;

import com.chair.chairdada.bigmodel.DeepSeekUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.IOException;

//@SpringBootTest
public class CodeTest {

//    @Resource
//    DeepSeekUtil deepSeekUtil;

    @Test
    public void test() throws IOException {
//        DeepSeekUtil.askDeepSeekSSE("你好，你能做什么？").subscribe(
//                chunk -> System.out.print(chunk), // 逐段输出
//                error -> System.err.println("Error: " + error),
//                () -> System.out.println("\nStream completed!")
//        );
        WebClient client = WebClient.create("http://localhost:11434");

        // 流式调用计算方法
        Flux<String> stream = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/generate")
                        .queryParam("model", "deepseek-r1:1.5b")
                        .queryParam("prompt", "你好，你能做什么？")
                        .queryParam("stream", true)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);

        // 订阅流式响应
        stream.subscribe(
                chunk -> System.out.print(chunk),  // 逐字输出
                error -> System.err.println("错误: " + error.getMessage()),
                () -> System.out.println("\n计算完成！")
        );

        // 防止主线程退出
        try { Thread.sleep(5000); }
        catch (InterruptedException e) { e.printStackTrace(); }


        // 整个返回
//        String s = deepSeekUtil.askDeepSeek("你好，你能做什么");
//        System.out.println(s);

    }
}
