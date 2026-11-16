package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.ShopItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShopItemMapper extends BaseMapper<ShopItem> {

    /** 各分类商品数量（商店筛选项） */
    @Select("SELECT category AS name, COUNT(*) AS cnt FROM shop_item GROUP BY category")
    List<Map<String, Object>> countByCategory();

    /**
     * 扣库存：不限量（stock=0）直接累加；限量时仅在还有余量时成功（返回受影响行数）。
     */
    @Update("UPDATE shop_item SET sold = sold + 1 "
            + "WHERE id = #{id} AND (stock = 0 OR sold < stock)")
    int increaseSold(Long id);
}
