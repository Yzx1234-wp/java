package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.CultivationLadder;
import com.fenglin.springboottest.controller.dto.CultivationVO;
import com.fenglin.springboottest.entity.CultivationLog;
import com.fenglin.springboottest.entity.Gongfa;
import com.fenglin.springboottest.entity.GongfaComprehend;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.entity.UserCultivation;
import com.fenglin.springboottest.mapper.CultivationLogMapper;
import com.fenglin.springboottest.mapper.GongfaComprehendMapper;
import com.fenglin.springboottest.mapper.UserCultivationMapper;
import com.fenglin.springboottest.service.CultivationService;
import com.fenglin.springboottest.service.GongfaService;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 「修炼境界」实现。
 * <p>核心规则（与积分体系完全解耦）：</p>
 * <ol>
 *   <li><b>修为</b>只来自「参悟功法」（每部典籍一生仅一次，品阶越高所得越多）
 *       与「打坐吐纳」（30 秒冷却，境界越高单次越多）；</li>
 *   <li>修为积满本境所需后 <b>不会自动升境</b>，须本人主动「冲击境界」渡劫；</li>
 *   <li>渡劫成功率 = 基础 60% + 已参悟功法数 × 2%（上限 92%），
 *       失败不跌境界但修为折损 25%，可再蓄力重冲。</li>
 * </ol>
 */
@Service
public class CultivationServiceImpl implements CultivationService {

    @Autowired
    private UserCultivationMapper cultivationMapper;

    @Autowired
    private GongfaComprehendMapper comprehendMapper;

    @Autowired
    private CultivationLogMapper logMapper;

    @Autowired
    private GongfaService gongfaService;

    @Autowired
    private UserService userService;

    @Override
    public CultivationVO overview(String username) {
        UserCultivation uc = ensure(username);
        int index = CultivationLadder.clampIndex(nvl(uc.getRealmIndex()));
        int exp = Math.max(0, nvl(uc.getExp()));
        int need = CultivationLadder.need(index);
        boolean top = CultivationLadder.isTop(index);

        Set<Long> comprehended = comprehendedIds(username);
        List<Gongfa> all = gongfaService.search(null, null, null, "rank");

        CultivationVO vo = new CultivationVO();
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        vo.setUsername(username);
        vo.setNickname(user == null || user.getNickname() == null || user.getNickname().isBlank()
                ? username : user.getNickname());

        vo.setTierIndex(index);
        vo.setTierName(CultivationLadder.name(index));
        vo.setTierTip(CultivationLadder.tip(index));
        vo.setExp(exp);
        vo.setNeed(need);
        vo.setProgress(top ? 100.0 : percent(exp, need));
        vo.setRemain(top ? 0 : Math.max(0, need - exp));
        vo.setCanBreak(!top && need > 0 && exp >= need);
        vo.setTop(top);
        vo.setNext(top ? null : CultivationLadder.name(index + 1));
        vo.setSuccessRate(CultivationLadder.successRate(comprehended.size()));
        vo.setComprehendCount(comprehended.size());
        vo.setComprehendTotal(all.size());
        vo.setBreakthroughCount(nvl(uc.getBreakthroughCount()));
        vo.setFailCount(nvl(uc.getFailCount()));
        vo.setTotalExp(nvl(uc.getTotalExp()));
        vo.setMeditateGain(CultivationLadder.meditateGain(index));
        vo.setMeditateCooldown(cooldownLeft(uc));
        vo.setMeditateCooldownTotal(CultivationLadder.MEDITATE_COOLDOWN);
        vo.setLadder(buildLadder(index));
        vo.setGongfa(buildGongfaItems(all, comprehended));
        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> comprehend(String username, Long gongfaId) {
        Gongfa g = gongfaService.getById(gongfaId);
        if (g == null) {
            throw new IllegalArgumentException("未找到该功法典籍，可能已被移除");
        }
        Long exists = comprehendMapper.selectCount(Wrappers.<GongfaComprehend>lambdaQuery()
                .eq(GongfaComprehend::getUsername, username)
                .eq(GongfaComprehend::getGongfaId, gongfaId));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("《" + g.getName() + "》已参悟过，同一部典籍只可参悟一次");
        }

        int gain = CultivationLadder.comprehendGain(g.getGrade());
        UserCultivation uc = ensure(username);

        GongfaComprehend record = new GongfaComprehend();
        record.setUsername(username);
        record.setGongfaId(gongfaId);
        record.setExpGain(gain);
        comprehendMapper.insert(record);

        uc.setExp(Math.max(0, nvl(uc.getExp())) + gain);
        uc.setTotalExp(nvl(uc.getTotalExp()) + gain);
        cultivationMapper.updateById(uc);

        addLog(username, CultivationLog.TYPE_COMPREHEND, "参悟《" + g.getName() + "》",
                (g.getGrade() == null ? "" : g.getGrade() + "典籍") + " · 得修为 +" + gain,
                gain, nvl(uc.getRealmIndex()));

        int index = CultivationLadder.clampIndex(nvl(uc.getRealmIndex()));
        Map<String, Object> res = progressPayload(username, uc);
        res.put("gain", gain);
        res.put("gongfaName", g.getName());
        res.put("tierName", CultivationLadder.name(index));
        return res;
    }

    @Override
    @Transactional
    public Map<String, Object> meditate(String username) {
        UserCultivation uc = ensure(username);
        int left = cooldownLeft(uc);
        if (left > 0) {
            throw new IllegalArgumentException("气息尚未平复，还需 " + left + " 秒方可再次吐纳");
        }
        int index = CultivationLadder.clampIndex(nvl(uc.getRealmIndex()));
        int gain = CultivationLadder.meditateGain(index);

        uc.setExp(Math.max(0, nvl(uc.getExp())) + gain);
        uc.setTotalExp(nvl(uc.getTotalExp()) + gain);
        uc.setLastMeditateAt(LocalDateTime.now());
        cultivationMapper.updateById(uc);

        addLog(username, CultivationLog.TYPE_MEDITATE, "打坐吐纳",
                "调息周天 · 得修为 +" + gain, gain, index);

        Map<String, Object> res = progressPayload(username, uc);
        res.put("gain", gain);
        res.put("meditateCooldown", CultivationLadder.MEDITATE_COOLDOWN);
        return res;
    }

    @Override
    @Transactional
    public Map<String, Object> breakthrough(String username) {
        UserCultivation uc = ensure(username);
        int index = CultivationLadder.clampIndex(nvl(uc.getRealmIndex()));
        if (CultivationLadder.isTop(index)) {
            throw new IllegalArgumentException("已至仙帝之境，大荒之上再无更高境界");
        }
        int need = CultivationLadder.need(index);
        int exp = Math.max(0, nvl(uc.getExp()));
        if (exp < need) {
            throw new IllegalArgumentException("修为不足，尚需 " + (need - exp) + " 修为方可冲击「"
                    + CultivationLadder.name(index + 1) + "境」");
        }

        int comprehended = comprehendedIds(username).size();
        int rate = CultivationLadder.successRate(comprehended);
        boolean success = ThreadLocalRandom.current().nextInt(100) < rate;
        String from = CultivationLadder.name(index);
        String to = CultivationLadder.name(index + 1);

        if (success) {
            int carry = exp - need;
            uc.setRealmIndex(index + 1);
            uc.setExp(carry);
            uc.setBreakthroughCount(nvl(uc.getBreakthroughCount()) + 1);
            cultivationMapper.updateById(uc);
            addLog(username, CultivationLog.TYPE_BREAKTHROUGH, "突破「" + to + "境」",
                    from + "境 → " + to + "境 · 渡劫成功（成功率 " + rate + "%）" + (carry > 0 ? "，结余修为 " + carry : ""),
                    0, index + 1);
        } else {
            int lost = (int) Math.floor(exp * (100 - CultivationLadder.FAIL_KEEP) / 100.0);
            int left = exp - lost;
            uc.setExp(left);
            uc.setFailCount(nvl(uc.getFailCount()) + 1);
            cultivationMapper.updateById(uc);
            addLog(username, CultivationLog.TYPE_FAIL, "冲击「" + to + "境」失败",
                    "渡劫未成，道基受损（成功率 " + rate + "%）· 修为 -" + lost, -lost, index);
        }

        Map<String, Object> res = progressPayload(username, uc);
        res.put("success", success);
        res.put("rate", rate);
        res.put("from", from);
        res.put("to", to);
        res.put("message", success
                ? "雷劫散尽，天地异象加身 —— 恭喜晋升「" + to + "境」！"
                : "道基受创，修为折损，凝神再战 —— 冲击「" + to + "境」未成");
        return res;
    }

    @Override
    public List<CultivationLog> logs(String username, int limit) {
        int size = limit <= 0 ? 20 : Math.min(limit, 100);
        return logMapper.selectList(Wrappers.<CultivationLog>lambdaQuery()
                .eq(CultivationLog::getUsername, username)
                .orderByDesc(CultivationLog::getCreatedAt)
                .orderByDesc(CultivationLog::getId)
                .last("LIMIT " + size));
    }

    /* ===== 私有辅助 ===== */

    /** 取修炼档案，首次访问时按「搬血境 · 修为 0」建档 */
    private UserCultivation ensure(String username) {
        UserCultivation uc = cultivationMapper.selectOne(Wrappers.<UserCultivation>lambdaQuery()
                .eq(UserCultivation::getUsername, username));
        if (uc != null) {
            return uc;
        }
        uc = new UserCultivation();
        uc.setUsername(username);
        uc.setRealmIndex(0);
        uc.setExp(0);
        uc.setTotalExp(0);
        uc.setBreakthroughCount(0);
        uc.setFailCount(0);
        cultivationMapper.insert(uc);
        return uc;
    }

    private Map<String, Object> progressPayload(String username, UserCultivation uc) {
        int index = CultivationLadder.clampIndex(nvl(uc.getRealmIndex()));
        int exp = Math.max(0, nvl(uc.getExp()));
        int need = CultivationLadder.need(index);
        boolean top = CultivationLadder.isTop(index);
        int comprehended = comprehendedIds(username).size();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("tierIndex", index);
        res.put("realmIndex", index);
        res.put("tierName", CultivationLadder.name(index));
        res.put("next", top ? null : CultivationLadder.name(index + 1));
        res.put("exp", exp);
        res.put("need", need);
        res.put("progress", top ? 100.0 : percent(exp, need));
        res.put("remain", top ? 0 : Math.max(0, need - exp));
        res.put("canBreak", !top && need > 0 && exp >= need);
        res.put("top", top);
        res.put("successRate", CultivationLadder.successRate(comprehended));
        res.put("comprehendCount", comprehended);
        res.put("totalExp", nvl(uc.getTotalExp()));
        res.put("breakthroughCount", nvl(uc.getBreakthroughCount()));
        res.put("failCount", nvl(uc.getFailCount()));
        res.put("meditateGain", CultivationLadder.meditateGain(index));
        res.put("meditateCooldown", cooldownLeft(uc));
        return res;
    }

    private List<CultivationVO.StepView> buildLadder(int current) {
        List<CultivationVO.StepView> list = new ArrayList<>();
        for (CultivationLadder.Step s : CultivationLadder.steps()) {
            CultivationVO.StepView v = new CultivationVO.StepView();
            v.setIndex(s.index());
            v.setName(s.name());
            v.setNeed(s.need());
            v.setTip(s.tip());
            v.setState(s.index() < current ? "done" : s.index() == current ? "current" : "locked");
            list.add(v);
        }
        return list;
    }

    private List<CultivationVO.ComprehendItem> buildGongfaItems(List<Gongfa> all, Set<Long> comprehended) {
        List<CultivationVO.ComprehendItem> items = new ArrayList<>();
        for (Gongfa g : all) {
            CultivationVO.ComprehendItem item = new CultivationVO.ComprehendItem();
            item.setId(g.getId());
            item.setName(g.getName());
            item.setCat(g.getCat());
            item.setGrade(g.getGrade());
            item.setGlyph(g.getGlyph());
            item.setTier(g.getTier());
            item.setRank(g.getRankScore());
            item.setGain(CultivationLadder.comprehendGain(g.getGrade()));
            item.setComprehended(comprehended.contains(g.getId()));
            items.add(item);
        }
        return items;
    }

    private Set<Long> comprehendedIds(String username) {
        return comprehendMapper.selectList(Wrappers.<GongfaComprehend>lambdaQuery()
                        .eq(GongfaComprehend::getUsername, username))
                .stream()
                .map(GongfaComprehend::getGongfaId)
                .collect(Collectors.toSet());
    }

    private void addLog(String username, String type, String title, String detail, int delta, int realmIndex) {
        CultivationLog log = new CultivationLog();
        log.setUsername(username);
        log.setType(type);
        log.setTitle(title);
        log.setDetail(detail);
        log.setExpDelta(delta);
        log.setRealmIndex(CultivationLadder.clampIndex(realmIndex));
        logMapper.insert(log);
    }

    /** 打坐吐纳冷却剩余秒数（不因 DATETIME 秒级截断而超出冷却总时长） */
    private int cooldownLeft(UserCultivation uc) {
        LocalDateTime last = uc.getLastMeditateAt();
        if (last == null) {
            return 0;
        }
        long passed = Duration.between(last, LocalDateTime.now()).getSeconds();
        long left = CultivationLadder.MEDITATE_COOLDOWN - passed;
        if (left <= 0) {
            return 0;
        }
        return (int) Math.min(left, CultivationLadder.MEDITATE_COOLDOWN);
    }

    private double percent(int exp, int need) {
        if (need <= 0) {
            return 100.0;
        }
        double p = exp * 100.0 / need;
        if (p > 100) {
            p = 100;
        }
        return Math.round(p * 10) / 10.0;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
