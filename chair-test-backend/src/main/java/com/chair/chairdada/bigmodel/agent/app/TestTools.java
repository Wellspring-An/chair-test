package com.chair.chairdada.bigmodel.agent.app;

import com.chair.chairdada.common.BaseResponse;
import com.chair.chairdada.controller.AppController;
import com.chair.chairdada.controller.QuestionController;
import com.chair.chairdada.model.dto.app.AppAddRequest;
import com.chair.chairdada.model.dto.question.AiGenerateQuestionRequest;
import com.chair.chairdada.model.dto.question.QuestionAddRequest;
import com.chair.chairdada.model.dto.question.QuestionContentDTO;
import com.chair.chairdada.model.entity.User;
import com.chair.chairdada.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestTools {

    private final AppController appController;
    private final QuestionController questionController;
    private final UserService userService;

    public TestTools(AppController appController,
                     QuestionController questionController,
                     UserService userService) {
        this.appController = appController;
        this.questionController = questionController;
        this.userService = userService;
    }

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();


    public static void setRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public static void clearRequest() {
        requestHolder.remove();
    }

    private HttpServletRequest getCurrentRequest() {
        HttpServletRequest request = requestHolder.get();
        if (request == null) {
            throw new RuntimeException("无法获取HTTP请求，请先设置请求上下文");
        }
        return request;
    }

    @Tool(description = "创建测试应用。根据用户提供的信息创建一个新的测试应用，返回应用ID")
    public String createApp(
            @ToolParam(description = "测试应用名称，例如：性格测试、能力测评") String appName,
            @ToolParam(description = "测试应用描述，简要说明测试的目的和内容") String appDesc,
            @ToolParam(description = "应用类型：0-得分类（有标准答案和分数），1-测评类（无标准答案，根据选项组合出结果）") Integer appType,
            @ToolParam(description = "评分策略：0-自定义评分规则，1-AI智能评分") Integer scoringStrategy,
            ToolContext toolContext
    ) {
        try {
            HttpServletRequest request = getCurrentRequest();

            if (request == null) {
                return "错误：无法获取用户登录信息，请先登录";
            }

            User loginUser = userService.getLoginUser(request);
            if (loginUser == null) {
                return "错误：用户未登录";
            }

            AppAddRequest appAddRequest = new AppAddRequest();
            appAddRequest.setAppName(appName);
            appAddRequest.setAppDesc(appDesc);
            appAddRequest.setAppType(appType);
            appAddRequest.setScoringStrategy(scoringStrategy);

            BaseResponse<Long> response = appController.addApp(appAddRequest, request);

            if (response.getCode() == 0) {
                Long appId = response.getData();
                return "✅ 成功创建测试应用！\n" +
                        "应用ID: " + appId + "\n" +
                        "应用名称: " + appName + "\n" +
                        "接下来可以使用 addQuestion 工具添加测试题目";
            } else {
                return "❌ 创建失败: " + response.getMessage();
            }
        } catch (Exception e) {
            return "❌ 创建应用时发生异常: " + e.getMessage();
        }
    }

    @Tool(description = "为测试应用添加题目")
    public String addQuestion(
            @ToolParam(description = "应用ID，要添加题目的测试应用ID") Long appId,
            @ToolParam(description = "题目数量") Integer questionNum,
            @ToolParam(description = "选项数量") Integer optionNum,
            ToolContext toolContext
    ) {
        try {
            HttpServletRequest request = getCurrentRequest();

            if (request == null) {
                return "错误：无法获取用户登录信息，请先登录";
            }

            User loginUser = userService.getLoginUser(request);
            if (loginUser == null) {
                return "错误：用户未登录";
            }

            AiGenerateQuestionRequest aiGenerateQuestionRequest = new AiGenerateQuestionRequest();
            aiGenerateQuestionRequest.setAppId(appId);
            aiGenerateQuestionRequest.setQuestionNumber(questionNum);
            aiGenerateQuestionRequest.setOptionNumber(optionNum);

            BaseResponse<List<QuestionContentDTO>> listBaseResponse = questionController.aiGenerateQuestion(aiGenerateQuestionRequest);

            QuestionAddRequest questionAddRequest = new QuestionAddRequest();
            questionAddRequest.setAppId(appId);
            questionAddRequest.setQuestionContent(listBaseResponse.getData());

            BaseResponse<Long> response = questionController.addQuestion(questionAddRequest, request);

            if (response.getCode() == 0) {
                return "✅ 成功添加题目，题目ID: " + response.getData();
            } else {
                return "❌ 添加失败: " + response.getMessage();
            }
        } catch (Exception e) {
            return "❌ 添加题目时发生异常: " + e.getMessage();
        }
    }
}