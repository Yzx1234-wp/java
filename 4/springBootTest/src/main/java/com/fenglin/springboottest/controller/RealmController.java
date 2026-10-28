package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.controller.dto.RealmSignupRequest;
import com.fenglin.springboottest.entity.SecretRealm;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.RealmService;
import com.fenglin.springboottest.service.TokenStore;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 「大荒秘境录」秘境接口。
 * 秘境浏览为公开数据；报名 / 取消报名需登录（Bearer token）。
 */
@RestController
@RequestMapping("/api/realms")
public class RealmController {

    @Autowired
    private RealmService realmService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /**
     * 秘境检索
     *
     * @param keyword 关键词，模糊匹配 名称/等级/所在地/开启时间/介绍
     * @param grade   秘境等级：仙帝 / 仙王 / 真仙 / 上古，「全部」或空为不筛
     * @param sortBy  排序：default（默认，评级递降）/ rank（按评级）/ name（按名称）
     */
    @GetMapping("/list")
    public Result<List<SecretRealm>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return Result.ok(realmService.search(keyword, grade, sortBy, currentUsername(auth)));
    }

    /** 等级筛选项（含数量） */
    @GetMapping("/grades")
    public Result<List<OptionVO>> grades() {
        return Result.ok(realmService.grades());
    }

    /** 热门秘境名称 */
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.ok(realmService.hotWords());
    }

    /** 秘境详情（带 token 时返回是否已报名） */
    @GetMapping("/{id}")
    public Result<SecretRealm> detail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        SecretRealm realm = realmService.detail(id, currentUsername(auth));
        if (realm == null) {
            return Result.fail("未找到该秘境");
        }
        return Result.ok(realm);
    }

    /** 我的报名列表（需登录） */
    @GetMapping("/my")
    public Result<List<SecretRealm>> my(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = requireUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看报名");
        }
        return Result.ok(realmService.myRealms(username));
    }

    /** 报名秘境（需登录，body: {"realmId": 1}） */
    @PostMapping("/signup")
    public Result<Map<String, Object>> signup(
            @RequestBody RealmSignupRequest req,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (req == null || req.getRealmId() == null) {
            return Result.fail("缺少秘境编号 realmId");
        }
        String username = requireUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再报名秘境");
        }
        try {
            int signedCount = realmService.signup(req.getRealmId(), username);
            return Result.ok(Map.of("realmId", req.getRealmId(), "signedCount", signedCount));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 取消报名（需登录） */
    @DeleteMapping("/signup/{realmId}")
    public Result<Void> cancel(
            @PathVariable Long realmId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = requireUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再操作报名");
        }
        realmService.cancelSignup(realmId, username);
        return Result.ok();
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

    private String requireUsername(String auth) {
        return currentUsername(auth);
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
