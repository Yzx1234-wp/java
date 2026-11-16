package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.ShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {

    /** 账号累计消耗积分（兑换记录即账本） */
    @Select("SELECT COALESCE(SUM(price), 0) FROM shop_order WHERE username = #{username}")
    int sumSpent(String username);

    /** 账号兑换总次数 */
    @Select("SELECT COUNT(*) FROM shop_order WHERE username = #{username}")
    int countByUser(String username);

    /** 账号各商品已兑换件数 */
    @Select("SELECT item_id AS itemId, COUNT(*) AS cnt FROM shop_order "
            + "WHERE username = #{username} GROUP BY item_id")
    List<Map<String, Object>> countByItem(String username);

    /** 全站累计消耗积分（后台备用） */
    @Select("SELECT COALESCE(SUM(price), 0) FROM shop_order")
    int sumAllSpent();
}
