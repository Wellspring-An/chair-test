package com.chair.chairdada.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ChatMessageEnums {

    SYSTEM_MESSAGE("系统消息", "systemMessage"),
    USER_MESSAGE("用户消息", "userMessage"),
    ADD_USER_MESSAGE("好友申请消息", "addUserMessage"),
    HEART_BEAT("心跳消息", "heartbeat");

    private final String text;

    private final String value;

    ChatMessageEnums(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ChatMessageEnums getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ChatMessageEnums anEnum : ChatMessageEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
