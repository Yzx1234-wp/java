package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秘境报名记录（每期每人限报一次，realm_id + username 唯一）。
 */
@Data
@TableName("realm_signup")
public class RealmSignup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 秘境 id */
    @TableField("realm_id")
    private Long realmId;

    /** 报名账号 */
    private String username;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signedAt;
}
