package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.CultivationLadder;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.AdminCultivationVO;
import com.fenglin.springboottest.entity.CultivationLog;
import com.fenglin.springboottest.entity.GongfaComprehend;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.entity.UserCultivation;
import com.fenglin.springboottest.mapper.CultivationLogMapper;
import com.fenglin.springboottest.mapper.GongfaComprehendMapper;
import com.fenglin.springboottest.mapper.UserCultivationMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理 · 修士境界（修炼档案）。
 * <p>前台「修炼境界」的配套管理：查看各修士境界/修为/参悟统计，
 * 支持管理员调整境界与修为（演示纠错，会留「天道干预」记录）、重置修炼档案。</p>
 */
@RestController
@RequestMapping("/api/admin/cultivations")
public class AdminCultivationController {

    /** 修炼记录类型：管理员调整（ CultivationLog 的 type 为 VARCHAR(16)，足够容纳） */
    public static final String LOG_TYPE_ADMIN = "ADMIN";

    @Autowired
    private UserService userService;

    @Autowired
    private UserCultivationMapper cultivationMapper;

    @Autowired
    private GongfaComprehendMapper comprehendMapper;

    @Autowired
    private CultivationLogMapper logMapper;

    @Autowired
    private AdminGuard adminGuard;

    /** 修士境界列表（全部账号，未入门的按「搬血 · 修为 0」展示） */
    @GetMapping("/list")
    public Result<List<AdminCultivationVO>> list(
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }

        var q = Wrappers.<User>lambdaQuery().orderByAsc(User::getId);
        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(User::getUsername, k).or().like(User::getNickname, k));
        }
        List<User> users = userService.list(q);

        Map<String, UserCultivation> profiles = new HashMap<>();
        for (UserCultivation uc : cultivationMapper.selectList(null)) {
            profiles.put(uc.getUsername(), uc);
        }
        Map<String, Integer> comprehendCnt = new HashMap<>();
        for (Map<String, Object> row : comprehendMapper.countByUser()) {
            comprehendCnt.put(String.valueOf(row.get("username")), toInt(row.get("cnt")));
        }

        List<AdminCultivationVO> list = new ArrayList<>();
        for (User u : users) {
            list.add(buildVO(u, profiles.get(u.getUsername()),
                    comprehendCnt.getOrDefault(u.getUsername(), 0)));
        }
        return Result.ok(list);
    }

    /** 调整境界与修为（演示纠错用；会写入「天道干预」修炼记录） */
    @PutMapping("/{username}")
    @Transactional
    public Result<AdminCultivationVO> adjust(
            @PathVariable String username,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            return Result.fail(404, "账号不存在，可能已被删除");
        }

        Integer realmIndex = body == null ? null : toInteger(body.get("realmIndex"));
        Integer exp = body == null ? null : toInteger(body.get("exp"));
        if (realmIndex == null || realmIndex < 0 || realmIndex >= CultivationLadder.size()) {
            return Result.fail(400, "境界序号需在 0-" + (CultivationLadder.size() - 1) + " 之间");
        }
        if (exp == null || exp < 0 || exp > 999999) {
            return Result.fail(400, "修为需为 0-999999 之间的整数");
        }

        UserCultivation uc = cultivationMapper.selectOne(Wrappers.<UserCultivation>lambdaQuery()
                .eq(UserCultivation::getUsername, username));
        int oldIndex = uc == null || uc.getRealmIndex() == null ? 0 : uc.getRealmIndex();
        if (uc == null) {
            uc = new UserCultivation();
            uc.setUsername(username);
            uc.setRealmIndex(realmIndex);
            uc.setExp(exp);
            uc.setTotalExp(exp);
            uc.setBreakthroughCount(Math.max(0, realmIndex));
            uc.setFailCount(0);
            cultivationMapper.insert(uc);
        } else {
            uc.setRealmIndex(realmIndex);
            uc.setExp(exp);
            cultivationMapper.updateById(uc);
        }
        // 回读以拿到 DB ON UPDATE 刷新后的 updated_at（插入分支 createdAt 同理）
        uc = cultivationMapper.selectOne(Wrappers.<UserCultivation>lambdaQuery()
                .eq(UserCultivation::getUsername, username));

        CultivationLog log = new CultivationLog();
        log.setUsername(username);
        log.setType(LOG_TYPE_ADMIN);
        log.setTitle("天道法则变动");
        log.setDetail(CultivationLadder.name(oldIndex) + "境 → " + CultivationLadder.name(realmIndex)
                + "境 · 管理员调整（本境修为 " + exp + "）");
        log.setExpDelta(0);
        log.setRealmIndex(realmIndex);
        logMapper.insert(log);

        return Result.ok(buildVO(user, uc,
                comprehendCountOf(username)));
    }

    /** 重置修炼档案（清除档案 / 参悟记录 / 修炼记录，一切从搬血重新开始） */
    @PostMapping("/{username}/reset")
    @Transactional
    public Result<Void> reset(@PathVariable String username, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            return Result.fail(404, "账号不存在，可能已被删除");
        }
        cultivationMapper.delete(Wrappers.<UserCultivation>lambdaQuery()
                .eq(UserCultivation::getUsername, username));
        comprehendMapper.delete(Wrappers.<GongfaComprehend>lambdaQuery()
                .eq(GongfaComprehend::getUsername, username));
        logMapper.delete(Wrappers.<CultivationLog>lambdaQuery()
                .eq(CultivationLog::getUsername, username));
        return Result.ok();
    }

    /* ===== 私有辅助 ===== */

    private AdminCultivationVO buildVO(User u, UserCultivation uc, int comprehendCount) {
        AdminCultivationVO vo = new AdminCultivationVO();
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAdmin(adminGuard.isAdmin(u.getUsername()));
        vo.setProfiled(uc != null);

        int index = CultivationLadder.clampIndex(uc == null || uc.getRealmIndex() == null ? 0 : uc.getRealmIndex());
        int exp = uc == null || uc.getExp() == null ? 0 : Math.max(0, uc.getExp());
        int need = CultivationLadder.need(index);
        boolean top = CultivationLadder.isTop(index);

        vo.setRealmIndex(index);
        vo.setRealmName(CultivationLadder.name(index));
        vo.setExp(exp);
        vo.setNeed(need);
        double progress = top || need <= 0 ? 100.0 : Math.min(100.0, Math.round(exp * 1000.0 / need) / 10.0);
        vo.setProgress(progress);
        vo.setTotalExp(uc == null || uc.getTotalExp() == null ? 0 : uc.getTotalExp());
        vo.setComprehendCount(comprehendCount);
        vo.setBreakthroughCount(uc == null || uc.getBreakthroughCount() == null ? 0 : uc.getBreakthroughCount());
        vo.setFailCount(uc == null || uc.getFailCount() == null ? 0 : uc.getFailCount());
        vo.setLastMeditateAt(uc == null ? null : uc.getLastMeditateAt());
        vo.setUpdatedAt(uc == null ? null : uc.getUpdatedAt());
        return vo;
    }

    private int comprehendCountOf(String username) {
        Long cnt = comprehendMapper.selectCount(Wrappers.<GongfaComprehend>lambdaQuery()
                .eq(GongfaComprehend::getUsername, username));
        return cnt == null ? 0 : cnt.intValue();
    }

    private Integer toInteger(Object o) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return o == null ? null : Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }
}
