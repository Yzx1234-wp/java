package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.ShopItem;
import com.fenglin.springboottest.entity.ShopOrder;
import com.fenglin.springboottest.mapper.ShopItemMapper;
import com.fenglin.springboottest.mapper.ShopOrderMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 后台管理 · 大荒宝库（兑换商店）。
 * <p>商品 CRUD 全部需管理员令牌；兑换记录为积分消耗账本，
 * 删除记录即收回该笔消耗（修士可用积分会相应回升）。</p>
 * 注意 ShopItem 的 JSON 字段名与前台一致：description→desc、hot→hot。
 */
@RestController
@RequestMapping("/api/admin/shop")
public class AdminShopController {

    /** 商品分类候选值（与前台商店页一致） */
    private static final List<String> CATEGORIES = List.of("丹药", "法宝", "武器", "高级功法");

    /** 品阶分级候选值 */
    private static final List<String> GRADES = List.of("绝世", "绝顶", "一流", "上乘", "中乘");

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopItemMapper itemMapper;

    @Autowired
    private ShopOrderMapper orderMapper;

    @Autowired
    private AdminGuard adminGuard;

    /* ===== 商品管理 ===== */

    /** 商品列表：支持关键词 / 分类 / 排序 */
    @GetMapping("/item/list")
    public Result<List<ShopItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        // 管理端不传 username：只算库存 / 售出，不掺入个人 owned / affordable
        return Result.ok(shopService.search(keyword, category, sortBy, null));
    }

    /** 分类候选值（供新增 / 编辑表单下拉使用） */
    @GetMapping("/categories")
    public Result<List<OptionVO>> categories(HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        List<OptionVO> options = new ArrayList<>();
        for (String c : CATEGORIES) {
            Long cnt = itemMapper.selectCount(Wrappers.<ShopItem>lambdaQuery().eq(ShopItem::getCategory, c));
            options.add(new OptionVO(c, c, cnt == null ? 0 : cnt.intValue()));
        }
        return Result.ok(options);
    }

    /** 新增商品 */
    @PostMapping("/item")
    public Result<ShopItem> create(@RequestBody ShopItem body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), null)) {
            return Result.fail(409, "商品名称「" + body.getName() + "」已存在，请换一个");
        }
        body.setId(null);
        body.setSold(0);
        body.setCreatedAt(LocalDateTime.now());
        itemMapper.insert(body);
        return Result.ok(itemMapper.selectById(body.getId()));
    }

    /** 修改商品 */
    @PutMapping("/item/{id}")
    public Result<ShopItem> update(@PathVariable Long id, @RequestBody ShopItem body,
                                   HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        ShopItem old = itemMapper.selectById(id);
        if (old == null) {
            return Result.fail(404, "未找到该商品，可能已被删除");
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), id)) {
            return Result.fail(409, "商品名称「" + body.getName() + "」已被其他商品占用");
        }
        body.setId(id);
        // 已售件数由兑换流程维护，创建时间不允许通过编辑修改
        body.setSold(null);
        body.setCreatedAt(null);
        itemMapper.updateById(body);
        return Result.ok(itemMapper.selectById(id));
    }

    /** 删除商品（历史兑换记录保留，积分账本不受影响） */
    @DeleteMapping("/item/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (itemMapper.selectById(id) == null) {
            return Result.fail(404, "未找到该商品，可能已被删除");
        }
        itemMapper.deleteById(id);
        return Result.ok();
    }

    /** 批量删除商品 */
    @PostMapping("/item/batch-delete")
    public Result<Integer> batchDelete(@RequestBody Map<String, List<Long>> body,
                                       HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        List<Long> ids = body == null ? null : body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.fail(400, "请先勾选要删除的商品");
        }
        int removed = itemMapper.delete(Wrappers.<ShopItem>lambdaQuery().in(ShopItem::getId, ids));
        return removed > 0 ? Result.ok(removed) : Result.fail(500, "批量删除失败，请重试");
    }

    /** 切换热门标记 */
    @PostMapping("/item/{id}/toggle-hot")
    public Result<Integer> toggleHot(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        ShopItem old = itemMapper.selectById(id);
        if (old == null) {
            return Result.fail(404, "未找到该商品，可能已被删除");
        }
        ShopItem patch = new ShopItem();
        patch.setId(id);
        patch.setHot(old.getHot() != null && old.getHot() == 1 ? 0 : 1);
        itemMapper.updateById(patch);
        return Result.ok(patch.getHot());
    }

    /* ===== 兑换记录 ===== */

    /** 全部兑换记录（keyword 模糊匹配 账号 / 商品名 / 分类） */
    @GetMapping("/orders")
    public Result<List<ShopOrder>> orders(
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        var q = Wrappers.<ShopOrder>lambdaQuery()
                .orderByDesc(ShopOrder::getCreatedAt)
                .orderByDesc(ShopOrder::getId)
                .last("LIMIT 200");
        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(ShopOrder::getUsername, k)
                    .or().like(ShopOrder::getItemName, k)
                    .or().like(ShopOrder::getCategory, k));
        }
        return Result.ok(orderMapper.selectList(q));
    }

    /**
     * 删除一条兑换记录。
     * <p>shop_order 同时是积分账本：可用积分 = 累计发放 - 本表 price 之和，
     * 因此删除记录会<b>收回该笔消耗</b>，修士可用积分相应回升；
     * 商品的已售件数同步减一。</p>
     */
    @DeleteMapping("/orders/{id}")
    public Result<Integer> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.fail(404, "未找到该兑换记录，可能已被删除");
        }
        orderMapper.deleteById(id);
        // 商品已售件数同步回退（限量商品库存随之回升）
        if (order.getItemId() != null) {
            ShopItem item = itemMapper.selectById(order.getItemId());
            if (item != null && item.getSold() != null && item.getSold() > 0) {
                ShopItem patch = new ShopItem();
                patch.setId(item.getId());
                patch.setSold(item.getSold() - 1);
                itemMapper.updateById(patch);
            }
        }
        return Result.ok(order.getPrice() == null ? 0 : order.getPrice());
    }

    /* ===== 内部工具 ===== */

    /** 统一去空格 / 补默认值 / 规范 hot 取值 */
    private void normalize(ShopItem s) {
        if (s.getName() != null) {
            s.setName(s.getName().trim());
        }
        if (s.getCategory() != null) {
            s.setCategory(s.getCategory().trim());
        }
        if (s.getGrade() != null) {
            s.setGrade(s.getGrade().trim());
        }
        if (s.getGlyph() == null || s.getGlyph().isBlank()) {
            s.setGlyph("✦");
        }
        if (s.getPrice() == null) {
            s.setPrice(0);
        }
        if (s.getStock() == null) {
            s.setStock(0);
        }
        if (s.getSortNo() == null) {
            s.setSortNo(0);
        }
        s.setHot(s.getHot() != null && s.getHot() == 1 ? 1 : 0);
    }

    /** 表单校验；返回 null 表示通过 */
    private String validate(ShopItem s) {
        String err = Validators.text(s.getName(), "商品名称", 50);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(s.getCategory(), "分类", CATEGORIES);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(s.getGrade(), "品阶分级", GRADES);
        if (err != null) {
            return err;
        }
        err = Validators.range(s.getPrice(), 0, 100000, "兑换积分");
        if (err != null) {
            return err;
        }
        err = Validators.range(s.getStock(), 0, 1000000, "库存件数");
        if (err != null) {
            return err;
        }
        err = Validators.range(s.getSortNo(), 0, 9999, "排序号");
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(s.getGlyph(), "卡面图标", 8);
        if (err != null) {
            return err;
        }
        return Validators.optionalText(s.getDescription(), "商品简介", 300);
    }

    /** 名称是否已被占用（excludeId 用于编辑时排除自身） */
    private boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        var q = Wrappers.<ShopItem>lambdaQuery().eq(ShopItem::getName, name);
        if (excludeId != null) {
            q.ne(ShopItem::getId, excludeId);
        }
        Long cnt = itemMapper.selectCount(q);
        return cnt != null && cnt > 0;
    }
}
