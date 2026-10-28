package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务接取记录（每项任务每人限接一次，接取即发积分；quest_id + username 唯一）。
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

    /** 接取时该任务的奖励积分（留档） */
    private Integer points;

    /** 接取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;
}
