package com.chair.chairdada.bigmodel.agent.weather;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.chair.chairdada.bigmodel.agent.app.TestTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherToolsTest {

    @Resource
    private WeatherTools weatherTools;

    @Test
    void testAgent() throws GraphRunnerException {
        weatherTools.toolUsage("北京");
    }

    @Resource
    private DeepSeekChatModel chatModel;

    // 在某个 Service 或 Controller 中使用
    @Resource
    private TestTools testTools;

    @Test
    public void createTestByAgent() throws GraphRunnerException {
        String userPrompt = "创建一个测试应用，名称为'性格测试'，描述为'通过回答一些问题来测试你的性格类型'，应用类型为得分，评分策略为Ai";

        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(testTools)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("test_creator_agent")
                .model(chatModel)
                .tools(provider.getToolCallbacks())
                .build();

        UserMessage userMessage = new UserMessage(userPrompt);
        String result = agent.call(userMessage).getText();
        System.out.println(result);
    }
}