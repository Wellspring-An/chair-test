package com.chair.chairdada.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Java 调用本地 Ollama DeepSeek-R1-8B 模型测试工具
 * 支持：非流式输出 / 流式输出
 * 依赖：JDK8+, 本地运行 Ollama + deepseek-r1:8b
 */
public class OllamaDeepSeekR1Test {

    // ====================== 配置项（你只需改这里）======================
    private static final String OLLAMA_API_URL = "https://2367w13f92.wicp.vip:29545/api/generate";
    private static final String MODEL_NAME = "deepseek-r1:8b";
    // ===============================================================

    public static void main(String[] args) {
        // 测试提问
        String prompt = "你好，简单介绍一下DeepSeek-R1模型，控制在100字";

        System.out.println("===== 非流式调用测试 =====");
        String nonStreamResult = callOllamaNonStream(prompt);
        System.out.println("模型回复：\n" + nonStreamResult);

        System.out.println("\n\n===== 流式调用测试 =====");
        callOllamaStream(prompt);
    }

    /**
     * 非流式调用（一次性返回全部结果）
     */
    public static String callOllamaNonStream(String prompt) {
        try {
            URL url = new URL(OLLAMA_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 构造请求JSON（stream: false = 非流式）
            String jsonInput = "{\n" +
                    "  \"model\": \"" + MODEL_NAME + "\",\n" +
                    "  \"prompt\": \"" + prompt.replace("\"", "\\\"") + "\",\n" +
                    "  \"stream\": false\n" +
                    "}";

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取响应
            int code = conn.getResponseCode();
            if (code != 200) {
                return "请求失败，状态码：" + code;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // 简单解析返回的response（只取response字段）
            String result = response.toString();
            if (result.contains("\"response\":\"")) {
                int start = result.indexOf("\"response\":\"") + 12;
                int end = result.indexOf("\"", start);
                return result.substring(start, end);
            }
            return result;

        } catch (Exception e) {
            return "调用异常：" + e.getMessage();
        }
    }

    /**
     * 流式调用（逐字输出，和ChatGPT一样打字效果）
     */
    public static void callOllamaStream(String prompt) {
        try {
            URL url = new URL(OLLAMA_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 流式请求JSON（stream: true）
            String jsonInput = "{\n" +
                    "  \"model\": \"" + MODEL_NAME + "\",\n" +
                    "  \"prompt\": \"" + prompt.replace("\"", "\\\"") + "\",\n" +
                    "  \"stream\": true\n" +
                    "}";

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            // 逐行读取流式响应
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                System.out.print("模型回复：");
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    // 解析每一行的token
                    if (line.contains("\"response\":\"")) {
                        int start = line.indexOf("\"response\":\"") + 12;
                        int end = line.indexOf("\"", start);
                        String token = line.substring(start, end);
                        System.out.print(token);
                        System.out.flush(); // 实时输出
                    }

                    // 结束标志
                    if (line.contains("\"done\":true")) {
                        System.out.println("\n【生成完成】");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("\n流式调用异常：" + e.getMessage());
        }
    }
}