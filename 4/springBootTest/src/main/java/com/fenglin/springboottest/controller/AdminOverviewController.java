package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.entity.RealmSignup;
import com.fenglin.springboottest.entity.SecretRealm;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.GongfaMapper;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.QuestMapper;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.mapper.SecretRealmMapper;
import com.fenglin.springboottest.mapper.WorldCharacterMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.CharacterService;
import com.fenglin.springboottest.service.GongfaService;
import com.fenglin.springboottest.service.QuestService;
import com.fenglin.springboottest.service.RealmService;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理 · 概览统计 / 当前管理员信息 / 分类字典。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GongfaService gongfaService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private RealmService realmService;

    @Autowired
    private QuestService questService;

    @Autowired
    private UserService userService;

    @Autowired
    private GongfaMapper gongfaMapper;

    @Autowired
    private WorldCharacterMapper characterMapper;

    @Autowired
    private SecretRealmMapper realmMapper;

    @Autowired
    private QuestMapper questMapper;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private AdminGuard adminGuard;

    /** 当前管理员信息（前端进入后台时校验令牌用） */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest request) {
        User admin = adminGuard.currentAdmin(request);
        if (admin == null) {
            Result<Void> auth = adminGuard.check(request);
            return Result.fail(auth == null ? 401 : auth.getCode(),
                    auth == null ? "未登录或登录已失效" : auth.getMessage());
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", admin.getId());
        m.put("username", admin.getUsername());
        m.put("nickname", admin.getNickname());
        m.put("createdAt", admin.getCreatedAt() == null ? null : admin.getCreatedAt().format(DATETIME));
        m.put("role", "admin");
        return Result.ok(m);
    }

    /** 后台首页概览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }

        Map<String, Object> m = new LinkedHashMap<>();
        long gongfaCount = gongfaService.count();
        long characterCount = characterService.count();
        long realmCount = realmService.count();
        long questCount = questService.count();
        long userCount = userService.count();
        long signupCount = signupMapper.selectCount(null);
        long acceptCount = acceptMapper.selectCount(null);

        // 只有后台点了「完成」的记录才计入已发放积分
        int questPoints = acceptMapper.sumAllPoints();
        int realmPoints = signupMapper.sumAllPoints();

        m.put("gongfaCount", gongfaCount);
        m.put("characterCount", characterCount);
        m.put("realmCount", realmCount);
        m.put("questCount", questCount);
        m.put("userCount", userCount);
        m.put("signupCount", signupCount);
        m.put("acceptCount", acceptCount);
        m.put("totalPoints", questPoints + realmPoints);
        m.put("questPoints", questPoints);
        m.put("realmPoints", realmPoints);
        m.put("pendingCount", acceptMapper.countAllPending() + signupMapper.countAllPending());
        m.put("completedCount", acceptMapper.countAllCompleted() + signupMapper.countAllCompleted());

        m.put("gongfaByCat", gongfaMapper.countByCat());
        m.put("characterByGrade", characterMapper.countByGrade());
        m.put("realmByGrade", realmMapper.countByGrade());
        m.put("questByGrade", questMapper.countByGrade());

        m.put("recentSignups", recentSignups());
        m.put("recentAccepts", recentAccepts());
        return Result.ok(m);
    }

    /** 最近 8 条秘境报名 */
    private List<Map<String, Object>> recentSignups() {
        List<RealmSignup> rows = signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                .orderByDesc(RealmSignup::getId).last("LIMIT 8"));
        Map<Long, String> names = new HashMap<>();
        List<Long> ids = rows.stream().map(RealmSignup::getRealmId).filter(java.util.Objects::nonNull).distinct().toList();
        if (!ids.isEmpty()) {
            for (SecretRealm r : realmService.listByIds(ids)) {
                names.put(r.getId(), r.getName());
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (RealmSignup s : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("username", s.getUsername());
            item.put("target", names.getOrDefault(s.getRealmId(), "已删除的秘境"));
            item.put("status", s.getStatus());
            item.put("points", s.getPoints() == null ? 0 : s.getPoints());
            item.put("time", s.getSignedAt() == null ? "" : s.getSignedAt().format(FMT));
            out.add(item);
        }
        return out;
    }

    /** 最近 8 条任务接取 */
    private List<Map<String, Object>> recentAccepts() {
        List<QuestAccept> rows = acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .orderByDesc(QuestAccept::getId).last("LIMIT 8"));
        Map<Long, Quest> quests = new HashMap<>();
        List<Long> ids = rows.stream().map(QuestAccept::getQuestId).filter(java.util.Objects::nonNull).distinct().toList();
        if (!ids.isEmpty()) {
            for (Quest q : questService.listByIds(ids)) {
                quests.put(q.getId(), q);
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (QuestAccept a : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            Quest q = quests.get(a.getQuestId());
            item.put("username", a.getUsername());
            item.put("target", q == null ? "已删除的任务" : q.getName());
            item.put("status", a.getStatus());
            item.put("points", a.getPoints() == null ? 0 : a.getPoints());
            item.put("time", a.getAcceptedAt() == null ? "" : a.getAcceptedAt().format(FMT));
            out.add(item);
        }
        return out;
    }
}
