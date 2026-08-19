package com.chair.chairdada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.chair.chairdada.model.dto.userFriends.UserFriendDto;
import com.chair.chairdada.model.entity.UserFriend;
import com.chair.chairdada.mapper.UserFriendMapper;
import com.chair.chairdada.model.vo.UserFriendVo;
import com.chair.chairdada.service.UserFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 好友关系表 服务实现类
 * </p>
 *
 * @author chair
 * @since 2026-08-13
 */
@Service
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> implements UserFriendService {

    @Resource
    private UserFriendMapper userFriendMapper;

    @Override
    public List<UserFriendVo> getFriendList(Long userId) {
        List<UserFriendVo> friendListVo = List.of();
        List<UserFriendDto> friendList = userFriendMapper.getFriendList(userId);
        if (friendList != null && !friendList.isEmpty()) {
            friendListVo = friendList.stream().map(userFriend -> {
                UserFriendVo userFriendVo = new UserFriendVo();
                BeanUtil.copyProperties(userFriend, userFriendVo);
                return userFriendVo;
            }).collect(Collectors.toList());
        }
        return friendListVo;
    }

    @Override
    public List<UserFriendVo> getNotFriendList(Long userId) {
        List<UserFriendVo> friendListVo = List.of();
        List<UserFriendDto> friendList = userFriendMapper.getNotFriendList(userId);
        if (friendList != null && !friendList.isEmpty()) {
            friendListVo = friendList.stream().map(userFriend -> {
                UserFriendVo userFriendVo = new UserFriendVo();
                BeanUtil.copyProperties(userFriend, userFriendVo);
                return userFriendVo;
            }).collect(Collectors.toList());
        }
        return friendListVo;
    }
}
