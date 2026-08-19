package com.chair.chairdada.mapper;

import com.chair.chairdada.model.dto.userFriends.UserFriendDto;
import com.chair.chairdada.model.entity.UserFriend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 好友关系表 Mapper 接口
 * </p>
 *
 * @author chair
 * @since 2026-08-13
 */
public interface UserFriendMapper extends BaseMapper<UserFriend> {

    public List<UserFriendDto> getFriendList(@Param("userId") Long userId);

    public List<UserFriendDto> getNotFriendList(@Param("userId") Long userId);
}