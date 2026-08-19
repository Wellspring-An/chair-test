package com.chair.chairdada.model.entity;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String message;
    private String type;
    private String sender;
    private String receiver;
    private String time;
}