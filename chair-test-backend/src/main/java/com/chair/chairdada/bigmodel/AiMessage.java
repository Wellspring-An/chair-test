package com.chair.chairdada.bigmodel;

import lombok.Data;

@Data
public class AiMessage {
    private String role;
    private String content;

    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
