package com.chair.chairdada.model.dto.user;

import lombok.Data;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import java.io.Serializable;

@Data
public class WxUserInfo extends WxOAuth2UserInfo implements Serializable {
    private String session_key;
}
