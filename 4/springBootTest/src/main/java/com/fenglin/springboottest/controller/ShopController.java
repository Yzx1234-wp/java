package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.controller.dto.ShopRedeemRequest;
import com.fenglin.springboottest.entity.ShopItem;
import com.fenglin.springboottest.entity.ShopOrder;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.ShopService;
import com.fenglin.springboottest.service.TokenStore;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 「大荒宝库 · 商店兑换」接口。
 * 商品浏览公开；查看钱包 / 兑换 / 我的兑换记录需登录（Bearer token）。
 * 积分口径：可用积分 = 任务+秘境累计发放 - 兑换消耗。
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /**
     * 商品检索
     *
     * @param keyword  关键词，模糊匹配 名称/分类/品阶/简介
     * @param category 分类：丹药 / 法宝 / 武器 / 高级功法，「全部」或空为不筛
     * @param sortBy   排序：default（热门优先、积分由低到高）/ priceAsc / priceDesc
     */
    @GetMapping("/list")
    public Result<List<ShopItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return Result.ok(shopService.search(keyword, category, sortBy, currentUsername(auth)));
    }

    /** 分类筛选项（含数量） */
    @GetMapping("/categories")
    public Result<List<OptionVO>> categories() {
        return Result.ok(shopService.categories());
    }

    /** 我的积分钱包（累计获得 / 已消耗 / 可用余额） */
    @GetMapping("/wallet")
    public Result<Map<String, Object>> wallet(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看积分");
        }
        return Result.ok(shopService.wallet(username));
    }

    /** 兑换商品（body: {"itemId": 1}） */
    @PostMapping("/redeem")
    public Result<Map<String, Object>> redeem(
            @RequestBody ShopRedeemRequest req,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (req == null || req.getItemId() == null) {
            return Result.fail("缺少商品编号 itemId");
        }
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再兑换");
        }
        try {
            return Result.ok(shopService.redeem(req.getItemId(), username));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 我的兑换记录 */
    @GetMapping("/orders")
    public Result<List<ShopOrder>> orders(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看兑换记录");
        }
        return Result.ok(shopService.myOrders(username));
    }

    /* ===== 私有辅助 ===== */

    /** 由 Bearer token 解析当前账号；未登录 / 失效返回 null */
    private String currentUsername(String auth) {
        String token = parseToken(auth);
        if (token == null || token.isBlank()) {
            return null;
        }
        Long uid = tokenStore.getUserId(token);
        if (uid == null) {
            return null;
        }
        User user = userService.getById(uid);
        return user == null ? null : user.getUsername();
    }

    private String parseToken(String auth) {
        if (auth == null) {
            return null;
        }
        String t = auth.trim();
        if (t.startsWith("Bearer ")) {
            return t.substring(7).trim();
        }
        if (t.startsWith("Bearer")) {
            return t.substring(6).trim();
        }
        return t;
    }
}
