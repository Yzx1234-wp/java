package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.ProfileVO;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.entity.RealmSignup;
import com.fenglin.springboottest.entity.SecretRealm;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.ProfileService;
import com.fenglin.springboottest.service.QuestService;
import com.fenglin.springboottest.service.RealmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 「我的主页」聚合实现。
 * 不新建表：全部由 star_user + quest_accept + realm_signup + quest + secret_realm 聚合而来。
 */
@Service
public class ProfileServiceImpl extends ServiceImpl<UserMapper, User> implements ProfileService {

    /** 修炼境界阶梯（《完美世界》标准 18 境，按总积分逐级解锁；等级越高积分越多） */
    private static final List<Map.Entry<String, Integer>> TIERS = List.of(
            Map.entry("搬血", 0),
            Map.entry("洞天", 100),
            Map.entry("化灵", 300),
            Map.entry("铭纹", 600),
            Map.entry("列阵", 900),
            Map.entry("尊者", 1200),
            Map.entry("神火", 1600),
            Map.entry("真一", 2000),
            Map.entry("圣祭", 2500),
            Map.entry("天神", 3000),
            Map.entry("虚道", 3600),
            Map.entry("斩我", 4200),
            Map.entry("遁一", 5000),
            Map.entry("至尊", 6000),
            Map.entry("真仙", 7000),
            Map.entry("仙王", 8000),
            Map.entry("准仙帝", 10000),
            Map.entry("仙帝", 15000)
    );

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private QuestService questService;

    @Autowired
    private RealmService realmService;

    @Override
    public ProfileVO summary(String username) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        ProfileVO vo = new ProfileVO();
        if (user == null) {
            vo.setUsername(username);
            vo.setNickname(username);
        } else {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() == null || user.getNickname().isBlank()
                    ? user.getUsername() : user.getNickname());
            vo.setCreatedAt(user.getCreatedAt());
        }

        int points = acceptMapper.sumPoints(username);
        long questCount = acceptMapper.selectCount(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getUsername, username));
        long realmCount = signupMapper.selectCount(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getUsername, username));

        vo.setTotalPoints(points);
        vo.setQuestCount((int) questCount);
        vo.setRealmCount((int) realmCount);
        vo.setTier(buildTier(points));
        vo.setBadges(buildBadges(points, (int) questCount, (int) realmCount,
                hasEmperorQuest(username)));
        vo.setBadgeCount((int) vo.getBadges().stream().filter(ProfileVO.Badge::getUnlocked).count());
        vo.setActivities(buildActivities(username));
        return vo;
    }

    @Override
    public List<Map<String, Object>> myQuests(String username) {
        List<QuestAccept> records = acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getUsername, username)
                .orderByDesc(QuestAccept::getAcceptedAt));
        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> ids = records.stream().map(QuestAccept::getQuestId).toList();
        Map<Long, Quest> quests = new HashMap<>();
        questService.listByIds(ids).forEach(q -> quests.put(q.getId(), q));

        List<Map<String, Object>> items = new ArrayList<>();
        for (QuestAccept r : records) {
            Quest q = quests.get(r.getQuestId());
            if (q == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questId", q.getId());
            item.put("name", q.getName());
            item.put("glyph", q.getGlyph());
            item.put("grade", q.getGrade());
            item.put("points", r.getPoints());
            item.put("poster", q.getPoster());
            item.put("desc", q.getDescription());
            item.put("acceptedAt", r.getAcceptedAt());
            items.add(item);
        }
        return items;
    }

    @Override
    public List<Map<String, Object>> myRealms(String username) {
        List<RealmSignup> records = signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getUsername, username)
                .orderByDesc(RealmSignup::getSignedAt));
        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> ids = records.stream().map(RealmSignup::getRealmId).toList();
        Map<Long, SecretRealm> realms = new HashMap<>();
        realmService.listByIds(ids).forEach(r -> realms.put(r.getId(), r));

        List<Map<String, Object>> items = new ArrayList<>();
        for (RealmSignup r : records) {
            SecretRealm realm = realms.get(r.getRealmId());
            if (realm == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("realmId", realm.getId());
            item.put("name", realm.getName());
            item.put("glyph", realm.getGlyph());
            item.put("grade", realm.getGrade());
            item.put("location", realm.getLocation());
            item.put("openTime", realm.getOpenTime());
            item.put("duration", realm.getDuration());
            item.put("desc", realm.getDescription());
            item.put("signedAt", r.getSignedAt());
            items.add(item);
        }
        return items;
    }

    /* ===== 私有辅助 ===== */

    /** 由总积分定位当前境界与下一境进度 */
    private ProfileVO.Tier buildTier(int points) {
        ProfileVO.Tier tier = new ProfileVO.Tier();
        int idx = 0;
        for (int i = 0; i < TIERS.size(); i++) {
            if (points >= TIERS.get(i).getValue()) {
                idx = i;
            }
        }
        tier.setName(TIERS.get(idx).getKey());
        tier.setIndex(idx);
        if (idx >= TIERS.size() - 1) {
            tier.setProgress(100.0);
            tier.setNext(null);
            tier.setNextPoints(null);
        } else {
            int cur = TIERS.get(idx).getValue();
            int next = TIERS.get(idx + 1).getValue();
            double progress = next == cur ? 100 : Math.round((points - cur) * 1000.0 / (next - cur)) / 10.0;
            tier.setProgress(progress);
            tier.setNext(TIERS.get(idx + 1).getKey());
            tier.setNextPoints(next);
        }
        return tier;
    }

    /** 是否接取过仙帝级任务 */
    private boolean hasEmperorQuest(String username) {
        List<QuestAccept> mine = acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getUsername, username));
        if (mine.isEmpty()) {
            return false;
        }
        Map<Long, Quest> map = new HashMap<>();
        questService.listByIds(mine.stream().map(QuestAccept::getQuestId).toList())
                .forEach(q -> map.put(q.getId(), q));
        return mine.stream().anyMatch(a -> {
            Quest q = map.get(a.getQuestId());
            return q != null && "仙帝".equals(q.getGrade());
        });
    }

    /** 成就徽章（含未点亮，前端灰显） */
    private List<ProfileVO.Badge> buildBadges(int points, int questCount, int realmCount, boolean emperorQuest) {
        List<ProfileVO.Badge> badges = new ArrayList<>();
        addBadge(badges, "start_q", "初入大荒", "首次接取悬赏任务", "🗡", questCount >= 1);
        addBadge(badges, "start_r", "探秘先锋", "首次报名大荒秘境", "🌌", realmCount >= 1);
        addBadge(badges, "p100", "积分新秀", "总积分达到 100", "✦", points >= 100);
        addBadge(badges, "p1000", "千分豪侠", "总积分达到 1000", "✦✦", points >= 1000);
        addBadge(badges, "emperor", "问鼎仙帝级", "接取过仙帝级悬赏", "⚡", emperorQuest);
        addBadge(badges, "q5", "悬赏猎人", "接取悬赏满 5 项", "🎯", questCount >= 5);
        addBadge(badges, "r3", "秘境常客", "报名秘境满 3 处", "🗺", realmCount >= 3);
        addBadge(badges, "p3000", "准帝之姿", "总积分达到 3000", "👑", points >= 3000);
        return badges;
    }

    private void addBadge(List<ProfileVO.Badge> badges, String key, String name, String desc,
                          String icon, boolean unlocked) {
        ProfileVO.Badge b = new ProfileVO.Badge();
        b.setKey(key);
        b.setName(name);
        b.setDesc(desc);
        b.setIcon(icon);
        b.setUnlocked(unlocked);
        badges.add(b);
    }

    /** 修炼动态：任务接取 + 秘境报名合并时间线（最近 12 条） */
    private List<ProfileVO.Activity> buildActivities(String username) {
        List<ProfileVO.Activity> list = new ArrayList<>();

        List<QuestAccept> accepts = acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getUsername, username)
                .orderByDesc(QuestAccept::getAcceptedAt)
                .last("LIMIT 12"));
        if (!accepts.isEmpty()) {
            Map<Long, Quest> map = new HashMap<>();
            questService.listByIds(accepts.stream().map(QuestAccept::getQuestId).toList())
                    .forEach(q -> map.put(q.getId(), q));
            for (QuestAccept a : accepts) {
                Quest q = map.get(a.getQuestId());
                if (q == null) {
                    continue;
                }
                ProfileVO.Activity act = new ProfileVO.Activity();
                act.setType("quest");
                act.setName(q.getName());
                act.setGlyph(q.getGlyph());
                act.setGrade(q.getGrade());
                act.setPoints(a.getPoints());
                act.setTime(a.getAcceptedAt());
                list.add(act);
            }
        }

        List<RealmSignup> signs = signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getUsername, username)
                .orderByDesc(RealmSignup::getSignedAt)
                .last("LIMIT 12"));
        if (!signs.isEmpty()) {
            Map<Long, SecretRealm> map = new HashMap<>();
            realmService.listByIds(signs.stream().map(RealmSignup::getRealmId).toList())
                    .forEach(r -> map.put(r.getId(), r));
            for (RealmSignup s : signs) {
                SecretRealm r = map.get(s.getRealmId());
                if (r == null) {
                    continue;
                }
                ProfileVO.Activity act = new ProfileVO.Activity();
                act.setType("realm");
                act.setName(r.getName());
                act.setGlyph(r.getGlyph());
                act.setGrade(r.getGrade());
                act.setTime(s.getSignedAt());
                list.add(act);
            }
        }

        list.sort(Comparator.comparing(ProfileVO.Activity::getTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list.size() > 12 ? list.subList(0, 12) : list;
    }
}
