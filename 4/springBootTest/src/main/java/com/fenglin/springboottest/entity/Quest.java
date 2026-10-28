package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 大荒悬赏任务。
 * <p>JSON 字段名与前端保持一致：isHot→hot；acceptedCount / accepted 为联表补充字段。</p>
 */
@Data
@TableName("quest")
public class Quest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务名称 */
    private String name;

    /** 卡面图标 */
    private String glyph;

    /** 任务等级：仙帝 / 仙王 / 真仙 / 上古（等级越高积分越多） */
    private String grade;

    /** 接取奖励积分 */
    private Integer points;

    /** 发布方（谁张贴的悬赏） */
    private String poster;

    /** 任务简介 */
    @TableField("description")
    @JsonProperty("desc")
    private String description;

    /** 是否热门任务：1 是 0 否 */
    @TableField("is_hot")
    @JsonProperty("hot")
    private Integer hot;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 已接取人数（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private Integer acceptedCount;

    /** 当前登录用户是否已接取（非数据库字段，带 token 查询时填充） */
    @TableField(exist = false)
    private Boolean accepted;
}
