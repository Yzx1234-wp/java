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
 * 大荒人物志 · 人物介绍。
 * <p>JSON 字段名与前端保持一致：rankScore→rank、quote 为主线语录。</p>
 */
@Data
@TableName("world_character")
public class WorldCharacter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人物姓名 */
    private String name;

    /** 称号（如 荒天帝） */
    private String title;

    /** 卡面图标 */
    private String glyph;

    /** 当前境界（仙帝 / 准仙帝 / 仙王 / 真仙 / 人道尊者 …） */
    private String realm;

    /** 出身 / 所属势力 */
    private String faction;

    /** 强度评分，用于「按实力」排序（前端字段 rank） */
    @TableField("rank_score")
    @JsonProperty("rank")
    private Double rankScore;

    /** 定位分级：仙帝 / 准仙帝 / 仙王 / 真仙 / 尊者 */
    private String grade;

    /** 经典语录 */
    private String quote;

    /** 人物简介 */
    private String description;

    /** 是否热门人物：1 是 0 否 */
    @TableField("is_hot")
    @JsonProperty("hot")
    private Integer hot;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
