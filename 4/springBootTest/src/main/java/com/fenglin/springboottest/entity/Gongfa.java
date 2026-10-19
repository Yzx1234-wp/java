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
 * 大荒功法典籍。
 * <p>JSON 字段名与前端首页保持一致：origin→from、description→desc、rankScore→rank。</p>
 */
@Data
@TableName("gongfa")
public class Gongfa {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 功法名称 */
    private String name;

    /** 一级分类：功法 / 宝术 / 至尊术 / 剑诀 / 秘法 / 身法 */
    private String cat;

    /** 品阶、流派简述 */
    private String tier;

    /** 卡面图标 */
    private String glyph;

    /** 传承来源（前端字段 from） */
    @TableField("origin")
    @JsonProperty("from")
    private String origin;

    /** 修习者 */
    private String owner;

    /** 品阶评分，用于「按品阶」排序（前端字段 rank） */
    @TableField("rank_score")
    @JsonProperty("rank")
    private Double rankScore;

    /** 品阶分级：绝世 / 绝顶 / 一流 / 上乘 / 中乘 */
    private String grade;

    /** 功法简介（前端字段 desc） */
    @TableField("description")
    @JsonProperty("desc")
    private String description;

    /** 是否热门典籍：1 是 0 否 */
    @TableField("is_hot")
    @JsonProperty("hot")
    private Integer hot;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
