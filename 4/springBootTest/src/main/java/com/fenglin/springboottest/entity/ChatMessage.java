package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 好友站内私信 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送人账号 */
    private String sender;

    /** 接收人账号 */
    private String receiver;

    /** 消息内容 */
    private String content;

    /** 是否已读：1 已读 / 0 未读 */
    private Integer isRead;

    /** 发送时间 */
    private LocalDateTime createdAt;
}
