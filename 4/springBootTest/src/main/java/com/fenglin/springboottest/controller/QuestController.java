package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.controller.dto.QuestAcceptRequest;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.QuestService;
import com.fenglin.springboottest.service.TokenStore;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 「大荒悬赏 · 任务接取」接口。
 * 任务浏览为公开数据；接取任务需登录（Bearer token），接取即发积分。
 */
@RestController
@RequestMapping("/api/quests")
public class QuestController {

    @Autowired
    private QuestService questService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /**
     * 任务检索
     *
     * @param keyword 关键词，模糊匹配 名称/等级/发布方/简介
     * @param grade   任务等级：仙帝 / 仙王 / 真仙 / 上古，「全部」或空为不筛
     * @param sortBy  排序：default（默认，积分递降）/ points（按积分）/ name（按名称）
     */
    @GetMapping("/list")
    public Result<List<Quest>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return Result.ok(questService.search(keyword, grade, sortBy, currentUsername(auth)));
    }

    /** 等级筛选项（含数量） */
    @GetMapping("/grades")
    public Result<List<OptionVO>> grades() {
        return Result.ok(questService.grades());
    }

    /** 热门任务名称 */
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.ok(questService.hotWords());
    }

    /** 任务详情（带 token 时返回是否已接取） */
    @GetMapping("/{id}")
    public Result<Quest> detail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Quest quest = questService.detail(id, currentUsername(auth));
        if (quest == null) {
            return Result.fail("未找到该任务");
        }
        return Result.ok(quest);
    }

    /** 我的总积分（需登录） */
    @GetMapping("/points")
    public Result<Map<String, Object>> points(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看积分");
        }
        return Result.ok(Map.of("username", username, "points", questService.myPoints(username)));
    }

    /** 我的接取列表（需登录） */
    @GetMapping("/my")
    public Result<List<Quest>> my(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再查看接取记录");
        }
        return Result.ok(questService.myQuests(username));
    }

    /** 接取任务（需登录，body: {"questId": 1}，接取即发积分） */
    @PostMapping("/accept")
    public Result<Map<String, Object>> accept(
            @RequestBody QuestAcceptRequest req,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (req == null || req.getQuestId() == null) {
            return Result.fail("缺少任务编号 questId");
        }
        String username = currentUsername(auth);
        if (username == null) {
            return Result.fail(401, "请先登录后再接取任务");
        }
        try {
            int gained = questService.accept(req.getQuestId(), username);
            return Result.ok(Map.of(
                    "questId", req.getQuestId(),
                    "gained", gained,
                    "totalPoints", questService.myPoints(username)));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
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
