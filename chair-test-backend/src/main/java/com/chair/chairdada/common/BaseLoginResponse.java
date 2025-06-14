package com.chair.chairdada.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用返回类
 */
public class BaseLoginResponse<T> extends BaseResponse<T> implements Serializable {

    @Getter
    @Setter
    private String token;

    public BaseLoginResponse(int code, T data, String message, String token) {
        super(code, data, message);
        this.token = token;
    }
}
