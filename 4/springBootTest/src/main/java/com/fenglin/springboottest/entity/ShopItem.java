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
 * 大荒宝库商品（丹药 / 法宝 / 武器 / 高级功法）。
 * <p>JSON 字段名与前端保持一致：description→desc、hot→hot；
 * owned / remain / soldOut / affordable 为查询时补充的非数据库字段。</p>
 */
@Data
@TableName("shop_item")
public class ShopItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 分类：丹药 / 法宝 / 武器 / 高级功法 */
    private String category;

    /** 品阶（绝世/绝顶/一流/上乘/中乘） */
    private String grade;

    /** 卡面图标 */
    private String glyph;

    /** 兑换所需积分 */
    private Integer price;

    /** 库存件数（0 表示不限量） */
    private Integer stock;

    /** 已兑换件数 */
    private Integer sold;

    /** 商品简介 */
    @TableField("description")
    @JsonProperty("desc")
    private String description;

    /** 排序号（越小越靠前） */
    private Integer sortNo;

    /** 是否热门：1 是 0 否 */
    @TableField("is_hot")
    @JsonProperty("hot")
    private Integer hot;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 当前账号已兑换件数（非数据库字段） */
    @TableField(exist = false)
    private Integer owned;

    /** 剩余库存（stock=0 不限量时返回 0；非数据库字段） */
    @TableField(exist = false)
    private Integer remain;

    /** 是否已兑完（非数据库字段） */
    @TableField(exist = false)
    private Boolean soldOut;

    /** 当前账号积分是否够兑换（非数据库字段） */
    @TableField(exist = false)
    private Boolean affordable;
}
