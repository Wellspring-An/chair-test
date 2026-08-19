package com.chair.chairdada.model.dto.userFriends;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 好友关系表
 * </p>
 *
 * @author chair
 * @since 2026-08-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_friend")
@Schema(name="UserFriend", description="好友关系表")
public class UserFriendDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(name = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(name = "好友ID")
    @TableField("friend_id")
    private Long friendId;

    @Schema(name = "好友账号")
    @TableField("userAccount")
    private String userAccount;

    @Schema(name = "好友昵称")
    @TableField("userName")
    private String userName;

    @Schema(name = "好友头像")
    @TableField("friendAvatar")
    private String friendAvatar;

    @Schema(name = "好友备注")
    @TableField("remark")
    private String remark;

    @Schema(name = "状态 0待同意 1已成为好友 2拒绝 3已删除 4拉黑")
    @TableField("status")
    private Integer status;

    @Schema(name = "申请好友留言")
    @TableField("apply_msg")
    private String applyMsg;

    @Schema(name = "申请时间")
    @TableField("apply_time")
    private LocalDateTime applyTime;

    @Schema(name = "同意好友时间")
    @TableField("agree_time")
    private LocalDateTime agreeTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;


}
