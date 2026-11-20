package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 好友申请（同一对人只保留一条，重复申请复用并改回 PENDING） */
@Data
@TableName("friend_request")
public class FriendRequest {

    /** 状态：待处理 */
    public static final String PENDING = "PENDING";
    /** 状态：已通过 */
    public static final String ACCEPTED = "ACCEPTED";
    /** 状态：已拒绝 */
    public static final String DECLINED = "DECLINED";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起人账号 */
    private String fromUser;

    /** 接收人账号 */
    private String toUser;

    /** 状态：PENDING 待处理 / ACCEPTED 已通过 / DECLINED 已拒绝 */
    private String status;

    /** 发起时间 */
    private LocalDateTime createdAt;
}
