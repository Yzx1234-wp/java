package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 好友关系（双向各一行，便于按 owner 直接查列表） */
@Data
@TableName("friend")
public class Friend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 持有人账号 */
    private String owner;

    /** 好友账号 */
    private String friend;

    /** 结交时间 */
    private LocalDateTime createdAt;
}
