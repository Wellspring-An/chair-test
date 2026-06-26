package com.chair.chairdada.bigmodel.param;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class TestInput {
    @ToolParam(description = "测试名称")
    private String testName;
    @ToolParam(description = "应用类型 1-测试类，0-评分类")
    private boolean appType;
    @ToolParam(description = "评分策略 1-Ai分析，0-自定义结果")
    private boolean scoringStrategy;
}
