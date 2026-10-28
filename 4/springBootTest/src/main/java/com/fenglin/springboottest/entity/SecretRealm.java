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
 * 大荒秘境。
 * <p>JSON 字段名与前端保持一致：rankScore→rank、isHot→hot；
 * signedCount / signed 为联表补充字段（报名人数 / 当前用户是否已报名）。</p>
 */
@Data
@TableName("secret_realm")
public class SecretRealm {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 秘境名称 */
    private String name;

    /** 卡面图标 */
    private String glyph;

    /** 秘境等级：仙帝 / 仙王 / 真仙 / 上古 */
    private String grade;

    /** 所在地 */
    private String location;

    /** 开启时间（如「每逢甲子 · 十月圆满之夜」） */
    @TableField("open_time")
    @JsonProperty("openTime")
    private String openTime;

    /** 开启时长（如「七日」） */
    private String duration;

    /** 每期名额（0 表示不限） */
    private Integer quota;

    /** 秘境评级评分，用于「按评级」排序（前端字段 rank） */
    @TableField("rank_score")
    @JsonProperty("rank")
    private Double rankScore;

    /** 秘境介绍 */
    @TableField("description")
    @JsonProperty("desc")
    private String description;

    /** 是否热门秘境：1 是 0 否 */
    @TableField("is_hot")
    @JsonProperty("hot")
    private Integer hot;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 已报名人数（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private Integer signedCount;

    /** 当前登录用户是否已报名（非数据库字段，带 token 查询时填充） */
    @TableField(exist = false)
    private Boolean signed;
}
