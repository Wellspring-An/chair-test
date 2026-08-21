package com.chair.chairdada.bigmodel.agent.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chair.chairdada.bigmodel.AiChatManager;
import com.chair.chairdada.common.BaseResponse;
import com.chair.chairdada.common.ErrorCode;
import com.chair.chairdada.common.ResultUtils;
import com.chair.chairdada.exception.ThrowUtils;
import com.chair.chairdada.model.dto.app.AppAddRequest;
import com.chair.chairdada.model.dto.question.AiGenerateQuestionRequest;
import com.chair.chairdada.model.dto.question.QuestionAddRequest;
import com.chair.chairdada.model.dto.question.QuestionContentDTO;
import com.chair.chairdada.model.entity.App;
import com.chair.chairdada.model.entity.Question;
import com.chair.chairdada.model.enums.AppTypeEnum;
import com.chair.chairdada.model.enums.ReviewStatusEnum;
import com.chair.chairdada.service.AppService;
import com.chair.chairdada.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.chair.chairdada.controller.QuestionController.GENERATE_QUESTION_SYSTEM_MESSAGE;

@Service
@Slf4j
public class TestTools {

    private final AppService appService;

    private final QuestionService questionService;

    private final AiChatManager aiManager;

    public TestTools(AppService appController,
                     QuestionService questionController,
                     AiChatManager aiManager) {
        this.appService = appController;
        this.questionService = questionController;
        this.aiManager = aiManager;
    }

    @Tool(description = "创建测试应用。根据用户提供的信息创建一个新的测试应用，返回应用ID")
    @Transactional
    public String createApp(
            @ToolParam(description = "测试应用名称，例如：性格测试、能力测评") String appName,
            @ToolParam(description = "测试应用描述，简要说明测试的目的和内容") String appDesc,
            @ToolParam(description = "应用类型：0-得分类（有标准答案和分数），1-测评类（无标准答案，根据选项组合出结果）") Integer appType,
            @ToolParam(description = "评分策略：0-自定义评分规则，1-AI智能评分") Integer scoringStrategy,
            ToolContext toolContext
    ) {
        try {
            long userId = (long) toolContext.getContext().get("userId");

            AppAddRequest appAddRequest = new AppAddRequest();
            appAddRequest.setAppName(appName);
            appAddRequest.setAppDesc(StringUtils.isBlank(appDesc) ? "http://localhost:8008/img/logo.a499c16d.png" : appDesc);
            appAddRequest.setAppType(appType);
            appAddRequest.setScoringStrategy(scoringStrategy);

            App app = new App();
            BeanUtil.copyProperties(appAddRequest, app);
            app.setUserId(userId);
            app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
            // 写入数据库
            boolean appResult = appService.save(app);
            // 返回新写入的数据 id
            long appId = app.getId();

            if (appResult) {
                return "✅ 成功创建测试应用！\n" +
                        "应用ID: " + appId + "\n" +
                        "应用名称: " + appName + "\n" +
                        "接下来可以使用 addQuestion 工具添加测试题目";
            } else {
                return "❌ 创建失败";
            }
        } catch (Exception e) {
            log.error("创建应用时发生异常: {}", e.getMessage());
            return "❌ 创建应用时发生异常: " + e.getMessage();
        }
    }

    @Tool(description = "更新测试应用。根据用户提供的信息更新测试应用")
    @Transactional
    public String updateApp(
            @ToolParam(description = "测试应用ID，例如：1") String appId,
            @ToolParam(description = "测试应用名称，例如：性格测试、能力测评") String appName,
            @ToolParam(description = "测试应用头像") String appIcon,
            @ToolParam(description = "测试应用描述，简要说明测试的目的和内容") String appDesc,
            @ToolParam(description = "应用类型：0-得分类（有标准答案和分数），1-测评类（无标准答案，根据选项组合出结果）") Integer appType,
            @ToolParam(description = "评分策略：0-自定义评分规则，1-AI智能评分") Integer scoringStrategy,
            ToolContext toolContext
    ) {
        try {
            long userId = (long) toolContext.getContext().get("userId");

            LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(App::getUserId, userId);
            queryWrapper.eq(App::getId, Long.parseLong(appId));

            App app = appService.getById(queryWrapper);

            if (app == null) {
                return "❌ 未找到该应用或不是您的应用";
            }

            App appAddRequest = new App();
            appAddRequest.setId(Long.parseLong(appId));
            appAddRequest.setAppName(StringUtils.isBlank(appName) ? app.getAppName() : appName);
            appAddRequest.setAppIcon(StringUtils.isBlank(appIcon) ? (StringUtils.isBlank(app.getAppIcon()) ? "http://site.chairabc.cloud/img/logo.a499c16d.png" : app.getAppIcon()) : appIcon);
            appAddRequest.setAppDesc(StringUtils.isBlank(appDesc) ? app.getAppDesc() : appDesc);
            appAddRequest.setAppType(null == appType ? app.getAppType() : appType);
            appAddRequest.setScoringStrategy(null == scoringStrategy ? app.getScoringStrategy() : scoringStrategy);

            boolean b = appService.updateById(appAddRequest);

            if (b) {
                return "✅ 成功更新测试应用！\n" +
                        "应用ID: " + appAddRequest.getId() + "\n" +
                        "应用名称: " + appName + "\n" +
                        "接下来可以使用 addQuestion 工具添加测试题目";
            } else {
                return "❌ 更新失败";
            }
        } catch (Exception e) {
            log.error("❌ 更新应用时发生异常: " + e.getMessage());
            return "❌ 更新应用时发生异常: " + e.getMessage();
        }
    }

    @Tool(description = "获取测试应用。根据用户提供的信息获取测试应用")
    public String getApp(
            ToolContext toolContext
    ) {
        try {
            long userId = (long) toolContext.getContext().get("userId");

            LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(App::getUserId, userId);
            List<App> list = appService.list(wrapper);

            List<String> result = new ArrayList<>();

            list.forEach(appVO -> {
                result.add("应用ID: " + appVO.getId() + "\n" +
                        "应用名称: " + appVO.getAppName() + "\n" +
                        "应用描述: " + appVO.getAppDesc() + "\n" +
                        "应用类型: " + appVO.getAppType() + "\n" +
                        "评分策略: " + appVO.getScoringStrategy() + "\n" +
                        "创建时间: " + appVO.getCreateTime() + "\n" +
                        "更新时间: " + appVO.getUpdateTime() + "\n" +
                        "---------------------------------\n");
            });

            if (!list.isEmpty()) {
                return "✅ 成功获取测试应用！\n" +
                        result + "\n" +
                        "接下来可以使用 addQuestion 工具添加测试题目";
            } else {
                return "❌ 获取失败";
            }
        } catch (Exception e) {
            log.error("❌ 更新应用时发生异常: " + e.getMessage());
            return "❌ 更新应用时发生异常: " + e.getMessage();
        }
    }

    @Tool(description = "为测试应用添加题目")
    @Transactional
    public String addQuestion(
            @ToolParam(description = "应用ID，要添加题目的测试应用ID") Long appId,
            @ToolParam(description = "题目数量") Integer questionNum,
            @ToolParam(description = "选项数量") Integer optionNum,
            ToolContext toolContext
    ) {
        try {
            long userId = (long) toolContext.getContext().get("userId");
            AiGenerateQuestionRequest aiGenerateQuestionRequest = new AiGenerateQuestionRequest();
            aiGenerateQuestionRequest.setAppId(appId);
            aiGenerateQuestionRequest.setQuestionNumber(questionNum);
            aiGenerateQuestionRequest.setOptionNumber(optionNum);

            BaseResponse<List<QuestionContentDTO>> listBaseResponse = AiGenerateQuestion(aiGenerateQuestionRequest);

            QuestionAddRequest questionAddRequest = new QuestionAddRequest();
            questionAddRequest.setAppId(appId);
            questionAddRequest.setQuestionContent(listBaseResponse.getData());

            Question question = new Question();
            BeanUtils.copyProperties(questionAddRequest, question);
            List<QuestionContentDTO> questionContentDTO = questionAddRequest.getQuestionContent();
            question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
            // 数据校验
            questionService.validQuestion(question, true);
            question.setUserId(userId);
            // 写入数据库
            boolean result = questionService.save(question);

            if (result) {
                return "✅ 成功添加题目，题目ID: " + question.getId();
            } else {
                return "❌ 添加失败";
            }
        } catch (Exception e) {
            log.error("❌ 添加题目时发生异常: " + e.getMessage());
            return "❌ 添加题目时发生异常: " + e.getMessage();
        }
    }

    @Tool(description = "创建测试应用，并创建应用的相关信息")
    public String addAppAllInfo(
            @ToolParam(description = "测试应用名称，例如：性格测试、能力测评") String appName,
            @ToolParam(description = "测试应用描述，简要说明测试的目的和内容") String appDesc,
            @ToolParam(description = "应用类型：0-得分类（有标准答案和分数），1-测评类（无标准答案，根据选项组合出结果）") Integer appType,
            @ToolParam(description = "评分策略：0-自定义评分规则，1-AI智能评分") Integer scoringStrategy,
            @ToolParam(description = "题目数量") Integer questionNum,
            @ToolParam(description = "选项数量") Integer optionNum,

            ToolContext toolContext
    ) {
        try {
            long userId = (long) toolContext.getContext().get("userId");

            AppAddRequest appAddRequest = new AppAddRequest();
            appAddRequest.setAppName(appName);
            appAddRequest.setAppDesc(StringUtils.isBlank(appDesc) ? "http://localhost:8008/img/logo.a499c16d.png" : appDesc);
            appAddRequest.setAppType(appType);
            appAddRequest.setScoringStrategy(scoringStrategy);

            App app = new App();
            BeanUtil.copyProperties(appAddRequest, app);
            app.setUserId(userId);
            app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
            // 写入数据库
            boolean appResult = appService.save(app);
            if (!appResult) {
                return "❌ 添加失败";
            }
            // 返回新写入的数据 id
            long appId = app.getId();

            AiGenerateQuestionRequest aiGenerateQuestionRequest = new AiGenerateQuestionRequest();
            aiGenerateQuestionRequest.setAppId(appId);
            aiGenerateQuestionRequest.setQuestionNumber(questionNum);
            aiGenerateQuestionRequest.setOptionNumber(optionNum);

            BaseResponse<List<QuestionContentDTO>> listBaseResponse = AiGenerateQuestion(aiGenerateQuestionRequest);

            QuestionAddRequest questionAddRequest = new QuestionAddRequest();
            questionAddRequest.setAppId(appId);
            questionAddRequest.setQuestionContent(listBaseResponse.getData());

            Question question = new Question();
            BeanUtils.copyProperties(questionAddRequest, question);
            List<QuestionContentDTO> questionContentDTO = questionAddRequest.getQuestionContent();
            question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
            // 数据校验
            questionService.validQuestion(question, true);
            question.setUserId(userId);
            // 写入数据库
            boolean result = questionService.save(question);

            if (result) {
                return "✅ 成功创建测试应用！\n" +
                        "应用ID: " + appId + "\n" +
                        "应用名称: " + appName + "\n";
            } else {
                return "❌ 添加失败";
            }
        } catch (Exception e) {
            log.error("❌ 添加题目时发生异常: " + e.getMessage());
            return "❌ 添加题目时发生异常: " + e.getMessage();
        }
    }

    public BaseResponse<List<QuestionContentDTO>> AiGenerateQuestion(
            AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        // 获取应用信息
        App app = appService.getById(appId);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getAppId, appId);
        Question question = questionService.getOne(queryWrapper);
        // 封装 Prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber, question);
        // AI 生成
        String result = aiManager.AiChat(userMessage, GENERATE_QUESTION_SYSTEM_MESSAGE);
        log.info("ai生成题目：{}", result);
        // 截取需要的 JSON 信息
        int start = result.indexOf("[");
        int end = result.lastIndexOf("]");
        String json = result.substring(start, end + 1);
        List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(json, QuestionContentDTO.class);
        return ResultUtils.success(questionContentDTOList);
    }

    /**
     * 生成题目的用户消息
     *
     * @param app
     * @param questionNumber
     * @param optionNumber
     * @return
     */
    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber, Question questionContent) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("应用名称:" + app.getAppName()).append("\n");
        userMessage.append("应用描述:" + app.getAppDesc()).append("\n");
        userMessage.append("已经有的题目:" + (null == questionContent ? "" : JSONUtil.toJsonStr(questionContent.getQuestionContent()))).append("\n");
        userMessage.append("应用类别:" + AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
        userMessage.append("要生成的题目数:" + questionNumber).append("\n");
        userMessage.append("每个题目的选项数:" + optionNumber);
        return userMessage.toString();
    }
}