package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宝库兑换记录，同时充当积分消耗账本：
 * 可用积分 = 任务/秘境累计发放积分 - 本表 price 之和。
 */
@Data
@TableName("shop_order")
public class ShopOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 兑换账号 */
    private String username;

    /** 商品 id */
    private Long itemId;

    /** 商品名称（留档） */
    private String itemName;

    /** 商品分类（留档） */
    private String category;

    /** 实际扣除的积分 */
    private Integer price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
