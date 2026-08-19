package com.chair.chairdada.service;

import com.chair.chairdada.model.entity.UserFriend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chair.chairdada.model.vo.UserFriendVo;

import java.util.List;

/**
 * <p>
 * 好友关系表 服务类
 * </p>
 *
 * @author chair
 * @since 2026-08-13
 */
public interface UserFriendService extends IService<UserFriend> {

    public List<UserFriendVo> getFriendList(Long userId);

    public List<UserFriendVo> getNotFriendList(Long userId);

}
