package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.ShopItem;
import com.fenglin.springboottest.entity.ShopOrder;
import com.fenglin.springboottest.mapper.FriendBattleMapper;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.mapper.ShopItemMapper;
import com.fenglin.springboottest.mapper.ShopOrderMapper;
import com.fenglin.springboottest.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopItemMapper, ShopItem> implements ShopService {

    /** 「全部」筛选项的 key / label，与前端保持一致 */
    private static final String ALL = "全部";

    /** 分类展示顺序 */
    private static final List<String> CAT_ORDER = List.of("丹药", "法宝", "武器", "高级功法");

    @Autowired
    private ShopOrderMapper orderMapper;

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private FriendBattleMapper battleMapper;

    @Override
    public List<ShopItem> search(String keyword, String category, String sortBy, String username) {
        LambdaQueryWrapper<ShopItem> q = Wrappers.lambdaQuery();

        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(ShopItem::getName, k)
                    .or().like(ShopItem::getCategory, k)
                    .or().like(ShopItem::getGrade, k)
                    .or().like(ShopItem::getDescription, k));
        }
        if (isActive(category)) {
            q.eq(ShopItem::getCategory, category.trim());
        }
        if ("priceDesc".equalsIgnoreCase(sortBy)) {
            q.orderByDesc(ShopItem::getPrice).orderByAsc(ShopItem::getId);
        } else if ("priceAsc".equalsIgnoreCase(sortBy)) {
            q.orderByAsc(ShopItem::getPrice).orderByAsc(ShopItem::getId);
        } else {
            // 默认：热门在前，其次按积分由低到高，让新人先看到买得起的
            q.orderByDesc(ShopItem::getHot)
                    .orderByAsc(ShopItem::getPrice)
                    .orderByAsc(ShopItem::getSortNo)
                    .orderByAsc(ShopItem::getId);
        }

        List<ShopItem> items = list(q);
        int available = username == null || username.isBlank() ? 0 : availablePoints(username);
        Map<Long, Integer> owned = ownedMap(username);
        for (ShopItem it : items) {
            int stock = nvl(it.getStock());
            int sold = nvl(it.getSold());
            it.setRemain(stock <= 0 ? 0 : Math.max(0, stock - sold));
            it.setSoldOut(stock > 0 && sold >= stock);
            it.setOwned(owned.getOrDefault(it.getId(), 0));
            it.setAffordable(available >= nvl(it.getPrice()));
        }
        return items;
    }

    @Override
    public List<OptionVO> categories() {
        Map<String, Integer> counts = new HashMap<>();
        int total = 0;
        for (Map<String, Object> row : baseMapper.countByCategory()) {
            Object name = row.get("name");
            if (name == null) {
                continue;
            }
            int cnt = toInt(row.get("cnt"));
            counts.put(String.valueOf(name), cnt);
            total += cnt;
        }

        List<OptionVO> options = new ArrayList<>();
        options.add(new OptionVO(ALL, ALL, total));
        for (String c : CAT_ORDER) {
            int n = counts.getOrDefault(c, 0);
            if (n > 0) {
                options.add(new OptionVO(c, c, n));
            }
        }
        return options;
    }

    @Override
    public Map<String, Object> wallet(String username) {
        int questPoints = acceptMapper.sumPoints(username);
        int realmPoints = signupMapper.sumPoints(username);
        int earned = questPoints + realmPoints;
        int spent = orderMapper.sumSpent(username);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", username);
        m.put("questPoints", questPoints);
        m.put("realmPoints", realmPoints);
        m.put("earned", earned);
        m.put("spent", spent);
        m.put("available", Math.max(0, earned - spent));
        m.put("orderCount", orderMapper.countByUser(username));
        return m;
    }

    @Override
    @Transactional
    public Map<String, Object> redeem(Long itemId, String username) {
        ShopItem item = getById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("未找到该商品，可能已下架");
        }
        int price = nvl(item.getPrice());
        int earned = acceptMapper.sumPoints(username) + signupMapper.sumPoints(username);
        int spent = orderMapper.sumSpent(username);
        int available = earned - spent;
        if (available < price) {
            throw new IllegalArgumentException("积分不足，「" + item.getName() + "」需 " + price
                    + " 积分，你还差 " + (price - available) + " 积分");
        }

        int stock = nvl(item.getStock());
        if (stock > 0 && nvl(item.getSold()) >= stock) {
            throw new IllegalArgumentException("「" + item.getName() + "」已被兑完，等下一期补货吧");
        }
        // 条件更新兜住并发抢购：受限量约束影响 0 行即视为已兑完
        if (baseMapper.increaseSold(itemId) == 0) {
            throw new IllegalArgumentException("「" + item.getName() + "」已被兑完，等下一期补货吧");
        }

        ShopOrder order = new ShopOrder();
        order.setUsername(username);
        order.setItemId(itemId);
        order.setItemName(item.getName());
        order.setCategory(item.getCategory());
        order.setPrice(price);
        orderMapper.insert(order);

        ShopItem fresh = getById(itemId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("itemId", itemId);
        m.put("itemName", item.getName());
        m.put("category", item.getCategory());
        m.put("price", price);
        m.put("earned", earned);
        m.put("spent", spent + price);
        m.put("available", available - price);
        m.put("owned", ownedMap(username).getOrDefault(itemId, 0));
        if (fresh != null) {
            int s = nvl(fresh.getStock());
            m.put("remain", s <= 0 ? 0 : Math.max(0, s - nvl(fresh.getSold())));
            m.put("soldOut", s > 0 && nvl(fresh.getSold()) >= s);
        }
        return m;
    }

    @Override
    public List<ShopOrder> myOrders(String username) {
        return orderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getUsername, username)
                .orderByDesc(ShopOrder::getCreatedAt)
                .orderByDesc(ShopOrder::getId)
                .last("LIMIT 50"));
    }

    @Override
    public int spentPoints(String username) {
        return username == null || username.isBlank() ? 0 : orderMapper.sumSpent(username);
    }

    /* ===== 私有辅助 ===== */

    /** 可用积分 = 累计发放 + 好友对战净得 - 兑换消耗 */
    private int availablePoints(String username) {
        int earned = acceptMapper.sumPoints(username) + signupMapper.sumPoints(username)
                + battleMapper.netDelta(username);
        return Math.max(0, earned - orderMapper.sumSpent(username));
    }

    /** 账号各商品已兑换件数 */
    private Map<Long, Integer> ownedMap(String username) {
        Map<Long, Integer> map = new HashMap<>();
        if (username == null || username.isBlank()) {
            return map;
        }
        for (Map<String, Object> row : orderMapper.countByItem(username)) {
            Object itemId = row.get("itemId");
            if (itemId == null) {
                continue;
            }
            map.put(Long.valueOf(String.valueOf(itemId)), toInt(row.get("cnt")));
        }
        return map;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private int toInt(Object v) {
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    /** 「全部」/ 空 视为不筛选 */
    private boolean isActive(String v) {
        return v != null && !v.isBlank() && !ALL.equals(v.trim()) && !"all".equalsIgnoreCase(v.trim());
    }
}
