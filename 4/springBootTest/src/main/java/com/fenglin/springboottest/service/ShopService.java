package com.fenglin.springboottest.service;

import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.ShopItem;
import com.fenglin.springboottest.entity.ShopOrder;

import java.util.List;
import java.util.Map;

/**
 * 「大荒宝库 · 商店兑换」。积分口径：
 * 可用积分 = 任务/秘境累计发放 - 兑换消耗（shop_order 记账）。
 */
public interface ShopService {

    /** 商品检索（带 token 时填充「我是否买得起 / 已兑换几件」） */
    List<ShopItem> search(String keyword, String category, String sortBy, String username);

    /** 分类筛选项（含数量） */
    List<OptionVO> categories();

    /** 我的积分钱包：累计获得 / 已消耗 / 可用余额 */
    Map<String, Object> wallet(String username);

    /** 兑换商品（扣积分、扣库存、写兑换记录） */
    Map<String, Object> redeem(Long itemId, String username);

    /** 我的兑换记录（最近 50 条） */
    List<ShopOrder> myOrders(String username);

    /** 账号累计消耗积分（供我的主页、任务页复用同一口径） */
    int spentPoints(String username);
}
