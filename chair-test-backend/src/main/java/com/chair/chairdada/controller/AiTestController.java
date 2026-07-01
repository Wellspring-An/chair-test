package com.chair.chairdada.controller;


import cn.hutool.core.date.DateTime;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson.JSON;
import com.chair.chairdada.annotation.AuthCheck;
import com.chair.chairdada.bigmodel.agent.app.TestTools;
import com.chair.chairdada.bigmodel.agent.hook.MessageTrimmingHook;
import com.chair.chairdada.common.BaseResponse;
import com.chair.chairdada.common.ErrorCode;
import com.chair.chairdada.common.ResultUtils;
import com.chair.chairdada.config.TokenConfig;
import com.chair.chairdada.exception.BusinessException;
import com.chair.chairdada.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class AiTestController {

    @Autowired
    @Qualifier("deepSeekChatClient")
    private ChatClient deepSeekChatClient;

    @Resource
    private DeepSeekChatModel chatModel;

    @Autowired
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TokenConfig tokenConfig;

    @PostMapping("/Ai/test")
    @AuthCheck(mustRole = "user")
    public String test(@RequestParam("userMessage") String userMessageStr, @RequestParam("systemMessage") String systemMessageStr) {
        DateTime now = DateTime.now();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String format = df.format(now);
        User userInfo = tokenConfig.getUserInfo();

        int count = redisTemplate.opsForValue().get(userInfo.getId() + format + "AiCount") == null ? 0 : (int) redisTemplate.opsForValue().get(userInfo.getId() + format + "AiCount");

        if (!userInfo.getUserRole().equals("admin")) {
            if (count >= 5) {
                return "AI调用次数超过限制，请明天再试";
            }
        }

        redisTemplate.opsForValue().set(userInfo.getId() + format + "AiCount", ++count, 1, TimeUnit.DAYS);

        UserMessage userMessage = new UserMessage(userMessageStr);
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);
        AssistantMessage assistantMessage = new AssistantMessage("```json\\n");
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage, assistantMessage),
                ChatOptions.builder().temperature(0.1).model("deepseek-v4-flash").stopSequences(List.of("```")).build());
        return deepSeekChatClient.prompt(prompt).advisors().call().content();
    }

    @PostMapping("/Ai/testStream")
    @AuthCheck(mustRole = "user")
    public Flux<ChatResponse> testStream(@RequestParam("userMessage") String userMessageStr, @RequestParam("systemMessage") String systemMessageStr) {
        User userInfo = tokenConfig.getUserInfo();

        DateTime now = DateTime.now();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String format = df.format(now);
        int count = redisTemplate.opsForValue().get(userInfo.getId() + format + "AiCount") == null ? 0 : (int) redisTemplate.opsForValue().get(userInfo.getId() + format + "AiCount");

        if (!userInfo.getUserRole().equals("admin")) {
            if (count >= 5) {
                return Flux.error(new BusinessException(ErrorCode.AI_COUNT_EXCEEDED));
            }
        }

        redisTemplate.opsForValue().set(userInfo.getId() + format + "AiCount", ++count, 1, TimeUnit.DAYS);

        UserMessage userMessage = new UserMessage(userMessageStr);
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);
        AssistantMessage assistantMessage = new AssistantMessage("```json\\n");
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage, assistantMessage),
                ChatOptions.builder().temperature(0.1).model("deepseek-v4-flash").stopSequences(List.of("```")).build());
        return deepSeekChatClient.prompt(prompt)
                .advisors(new SimpleLoggerAdvisor()).stream().chatResponse();
    }

    @Resource
    private TestTools testTools;

    @PostMapping("/Ai/testAddApp")
    @AuthCheck(mustRole = "user")
    public BaseResponse<String> testAddApp(@RequestParam("userMessage") String userPrompt) throws GraphRunnerException {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        TestTools.setRequest(request);

        User userInfo = tokenConfig.getUserInfo();

        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(testTools)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("test_creator_agent")
                .model(chatModel)
                .tools(provider.getToolCallbacks())
                .hooks(new MessageTrimmingHook())
                .saver(new MemorySaver())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(userInfo.getUserAccount() + userInfo.getId())
                .build();

        UserMessage userMessage = new UserMessage(userPrompt);
        String result = agent.call(userMessage, config).getText();
        return ResultUtils.success(result);
    }

    @PostMapping(value = "/Ai/testAddAppStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthCheck(mustRole = "user")
    public Flux<String> testAddAppStream(@RequestParam("userMessage") String userPrompt) throws GraphRunnerException {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        TestTools.setRequest(request);

        User userInfo = tokenConfig.getUserInfo();
        String threadId = userInfo.getUserAccount() + "_" + userInfo.getId();

        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(testTools)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("test_creator_agent")
                .model(chatModel)
                .tools(provider.getToolCallbacks())
                .hooks(new MessageTrimmingHook())
                .saver(new MemorySaver())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        UserMessage userMessage = new UserMessage(userPrompt);
        Flux<NodeOutput> nodeOutputFlux = agent.stream(userMessage, config);

        return nodeOutputFlux
                .flatMap(nodeOutput -> {
                    // 改用你原有StreamingOutput判断取文本
                    List<Message> messages = new ArrayList<>();
                    if (nodeOutput instanceof StreamingOutput streamingOutput) {
                        OutputType type = streamingOutput.getOutputType();
                        if (type == OutputType.AGENT_MODEL_STREAMING) {
                            messages.add(streamingOutput.message());
                        }
                    }
                    return Flux.fromIterable(messages);
                })
                .filter(text -> text != null && !text.getText().isEmpty())
                // 关键：包装标准SSE+合法JSON对象
                .map(text -> {
                    // 构造和你报错结构一致的对象
                    Map<String, Object> contentMap = Map.of(
                            "media", List.of(),
                            "messageType", "ASSISTANT",
                            "metadata", Map.of(
                                    "finishReason", "",
                                    "role", "ASSISTANT",
                                    "id", UUID.randomUUID().toString(),
                                    "messageType", "ASSISTANT"
                            ),
                            "text", text,
                            "toolCalls", List.of()
                    );
                    Map<String, Object> fullData = Map.of("content", contentMap);
                    // 标准SSE行，双换行结尾
                    return "data: " + JSON.toJSONString(fullData) + "\n\n";
                });
    }
}
