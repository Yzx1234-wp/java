package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务接取记录（每项任务每人限接一次，quest_id + username 唯一）。
 * <p>接取后为「待完成」（PENDING），只有后台管理点「完成」才置为 COMPLETED 并计入积分。</p>
 */
@Data
@TableName("quest_accept")
public class QuestAccept {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务 id */
    @TableField("quest_id")
    private Long questId;

    /** 接取账号 */
    private String username;

    /** 奖励积分：接取时留档，后台确认完成时按任务最新积分写入 */
    private Integer points;

    /** 完成状态：PENDING 待完成 / COMPLETED 已完成（已完成才计入总积分） */
    private String status;

    /** 接取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;

    /** 后台确认完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /** 确认完成的管理员账号 */
    @TableField("completed_by")
    private String completedBy;
}
