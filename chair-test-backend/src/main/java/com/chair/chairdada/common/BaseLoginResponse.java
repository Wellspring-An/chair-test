package com.chair.chairdada.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @param <T>
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
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
