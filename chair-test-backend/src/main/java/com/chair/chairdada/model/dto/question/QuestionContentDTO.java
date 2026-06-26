package com.chair.chairdada.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class QuestionContentDTO {
    /**
     * 题目标题
     */
    @ToolParam(description = "题目标题")
    private String title;
    /**
     * 题目选项列表
     */
    @ToolParam(description = "题目选项列表")
    private List<Option> options;

    /**
     * 题目选项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        @ToolParam(description = "选项内容")
        private String value;
        @ToolParam(description = "选项分数")
        private int score;
        @ToolParam(description = "选项值")
        private String key;
    }

}
