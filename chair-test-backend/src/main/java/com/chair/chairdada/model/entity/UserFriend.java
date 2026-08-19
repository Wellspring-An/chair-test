package com.chair.chairdada.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
public class UserFriend implements Serializable {

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
