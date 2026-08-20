package com.chair.chairdada.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chair.chairdada.annotation.AuthCheck;
import com.chair.chairdada.common.BaseResponse;
import com.chair.chairdada.common.ErrorCode;
import com.chair.chairdada.common.ResultUtils;
import com.chair.chairdada.config.TokenConfig;
import com.chair.chairdada.constant.UserConstant;
import com.chair.chairdada.exception.BusinessException;
import com.chair.chairdada.model.entity.User;
import com.chair.chairdada.model.entity.UserFriend;
import com.chair.chairdada.model.vo.UserFriendVo;
import com.chair.chairdada.service.UserFriendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 好友关系表 前端控制器
 * </p>
 *
 * @author chair
 * @since 2026-08-13
 */
@Slf4j
@RestController
@RequestMapping("/userFriend")
public class UserFriendController {

    @Resource
    private UserFriendService userFriendService;

    @Resource
    private TokenConfig tokenConfig;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse addFriend(@RequestBody UserFriendVo userFriendVo) {
        User userInfo = tokenConfig.getUserInfo();
        userFriendVo.setUserId(userInfo.getId());
        UserFriend userFriend = new UserFriend();
        BeanUtil.copyProperties(userFriendVo, userFriend);
        userFriend.setCreateTime(LocalDateTime.now());
        userFriend.setApplyTime(LocalDateTime.now());
        userFriend.setStatus(0);
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getUserId, userFriend.getUserId());
        queryWrapper.eq(UserFriend::getFriendId, userFriend.getFriendId());
        UserFriend one = userFriendService.getOne(queryWrapper);
        if (one != null) {
            return ResultUtils.error(ErrorCode.ADD_FRIEND_ERROR);
        }
        // 添加好友逻辑
        boolean save = userFriendService.save(userFriend);
        return ResultUtils.success(save);
    }

    @PostMapping("/updateById")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse updateFriendById(@RequestBody UserFriendVo userFriendVo) {
        User userInfo = tokenConfig.getUserInfo();
        userFriendVo.setUserId(userInfo.getId());
        UserFriend userFriend = new UserFriend();
        BeanUtil.copyProperties(userFriendVo, userFriend);
        userFriend.setAgreeTime(LocalDateTime.now());
        userFriend.setStatus(userFriendVo.getStatus());
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getId, userFriend.getId());
        boolean b;
        try {
            b = userFriendService.updateById(userFriend);
        }catch (Exception e) {
            log.error("更新好友关系时发生异常: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (!b) {
            return ResultUtils.error(ErrorCode.UPDATE_FRIEND_ERROR);
        }
        return ResultUtils.success(b);
    }

    @PostMapping("/selectAll")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<UserFriendVo>> selectAllFriend() {
        User currentUser = tokenConfig.getUserInfo();
        // 假设用户ID一定存在，或者上面已经做了判空
        List<UserFriendVo> friendList = userFriendService.getFriendList(currentUser.getId());
        return ResultUtils.success(friendList);
    }

    @PostMapping("/selectAddAll")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<UserFriendVo>> selectAddAllFriend() {
        User currentUser = tokenConfig.getUserInfo();
        // 假设用户ID一定存在，或者上面已经做了判空
        List<UserFriendVo> friendList = userFriendService.getNotFriendList(currentUser.getId());
        return ResultUtils.success(friendList);
    }
}
