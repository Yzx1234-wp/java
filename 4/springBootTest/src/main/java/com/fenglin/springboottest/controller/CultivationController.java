package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.CultivationVO;
import com.fenglin.springboottest.entity.CultivationLog;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.CultivationService;
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
 * 「修炼境界」接口。全部需登录（Bearer token）。
 * <p>境界不由积分决定：修为来自参悟功法 / 打坐吐纳，积满后须主动冲击境界渡劫。</p>
 */
@RestController
@RequestMapping("/api/cultivation")
public class CultivationController {

    @Autowired
    private CultivationService cultivationService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /** 修炼总览（境界 / 修为进度 / 渡劫成功率 / 十八境阶梯 / 可参悟典籍） */
    @GetMapping("/overview")
    public Result<CultivationVO> overview(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再入问道台");
        }
        return Result.ok(cultivationService.overview(username));
    }

    /** 参悟功法（body: {"gongfaId": 1}；每部典籍仅可参悟一次） */
    @PostMapping("/comprehend")
    public Result<Map<String, Object>> comprehend(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再参悟功法");
        }
        Object raw = body == null ? null : body.get("gongfaId");
        if (raw == null) {
            return Result.fail("缺少功法编号 gongfaId");
        }
        Long gongfaId;
        try {
            gongfaId = Long.parseLong(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return Result.fail("功法编号 gongfaId 格式不正确");
        }
        try {
            return Result.ok(cultivationService.comprehend(username, gongfaId));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 打坐吐纳（30 秒冷却，境界越高单次所得越多） */
    @PostMapping("/meditate")
    public Result<Map<String, Object>> meditate(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再打坐吐纳");
        }
        try {
            return Result.ok(cultivationService.meditate(username));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 冲击境界（渡劫）：成功晋升下一境，失败修为折损 25% */
    @PostMapping("/breakthrough")
    public Result<Map<String, Object>> breakthrough(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再冲击境界");
        }
        try {
            return Result.ok(cultivationService.breakthrough(username));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 修炼记录（参悟 / 吐纳 / 突破 / 失败） */
    @GetMapping("/logs")
    public Result<List<CultivationLog>> logs(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看修炼记录");
        }
        return Result.ok(cultivationService.logs(username, limit == null ? 20 : limit));
    }

    /* ===== 私有辅助 ===== */

    /** 由 Bearer token 解析当前账号；未登录 / 失效返回 null（TokenStore 不接受 null，必须先判空） */
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
