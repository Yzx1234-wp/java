package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.ProfileVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.ProfileService;
import com.fenglin.springboottest.service.TokenStore;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 「我的主页」接口。全部需登录（Bearer token）。
 * 聚合：总积分 / 已接任务 / 已报秘境 / 修炼境界 / 成就徽章 / 修炼动态。
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /** 主页总览（账号 + 积分 + 境界进度 + 成就徽章 + 修炼动态） */
    @GetMapping("/summary")
    public Result<ProfileVO> summary(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看我的主页");
        }
        return Result.ok(profileService.summary(username));
    }

    /** 我接取的任务明细（含接取时间与到账积分） */
    @GetMapping("/quests")
    public Result<List<Map<String, Object>>> myQuests(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看接取记录");
        }
        return Result.ok(profileService.myQuests(username));
    }

    /** 我报名的秘境明细（含报名时间与开启时间） */
    @GetMapping("/realms")
    public Result<List<Map<String, Object>>> myRealms(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看报名记录");
        }
        return Result.ok(profileService.myRealms(username));
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
