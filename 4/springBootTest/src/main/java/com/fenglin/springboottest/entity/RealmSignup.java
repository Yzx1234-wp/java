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
 * <p>报名后为「待完成」（PENDING），只有后台管理点「完成」才置为 COMPLETED 并计入积分。</p>
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

    /** 通关奖励积分：报名时留档，后台确认完成时按秘境最新积分写入 */
    private Integer points;

    /** 完成状态：PENDING 待完成 / COMPLETED 已完成（已完成才计入总积分） */
    private String status;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signedAt;

    /** 后台确认完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /** 确认完成的管理员账号 */
    @TableField("completed_by")
    private String completedBy;
}
