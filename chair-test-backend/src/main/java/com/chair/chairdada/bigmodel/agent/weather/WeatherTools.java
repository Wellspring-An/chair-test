package com.chair.chairdada.bigmodel.agent.weather;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.chair.chairdada.bigmodel.param.WeatherInput;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
public class WeatherTools implements BiFunction<WeatherInput, ToolContext, String> {

    public int counter = 0;

    @Resource
    private DeepSeekChatModel chatModel;

    public WeatherTools() {
    }

    @Override
    public String apply(
            WeatherInput location,
            ToolContext toolContext) {
        counter++;
        System.out.println("Weather tool called : " + location.getLocation());
        return "Sunny, 25°C, light breeze from the east.";
    }

    public void toolUsage(String city) throws GraphRunnerException {

        // 创建工具回调：移除 inputType(Object.class)
        ToolCallback searchTool = FunctionToolCallback
                .builder("weather", new WeatherTools())
                .description("天气信息的工具，用于查询指定城市实时天气")
                .inputType(WeatherInput.class)
                .build();

        // 使用多个工具
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(searchTool)
                .build();

        UserMessage userMessage = new UserMessage(city + "天气怎么样？");
        String text = agent.call(userMessage).getText();
        System.out.println(text);
    }
}