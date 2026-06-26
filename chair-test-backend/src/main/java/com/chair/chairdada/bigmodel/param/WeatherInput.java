package com.chair.chairdada.bigmodel.param;

import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherInput {
    @ToolParam(description = "查询天气情况的位置信息")
    private String location;

    // getter setter
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}
