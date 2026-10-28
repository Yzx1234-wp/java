package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.RealmSignup;
import com.fenglin.springboottest.entity.SecretRealm;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.mapper.SecretRealmMapper;
import com.fenglin.springboottest.service.RealmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RealmServiceImpl extends ServiceImpl<SecretRealmMapper, SecretRealm> implements RealmService {

    /** 「全部」筛选项的 key / label，与前端保持一致 */
    private static final String ALL = "全部";

    /** 秘境等级展示顺序（由高到低） */
    private static final List<String> GRADE_ORDER = List.of("仙帝", "仙王", "真仙", "上古");

    @Autowired
    private RealmSignupMapper signupMapper;

    @Override
    public List<SecretRealm> search(String keyword, String grade, String sortBy, String username) {
        LambdaQueryWrapper<SecretRealm> q = Wrappers.lambdaQuery();

        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(SecretRealm::getName, k)
                    .or().like(SecretRealm::getGrade, k)
                    .or().like(SecretRealm::getLocation, k)
                    .or().like(SecretRealm::getOpenTime, k)
                    .or().like(SecretRealm::getDescription, k));
        }
        if (isActive(grade)) {
            q.eq(SecretRealm::getGrade, grade);
        }

        if ("name".equalsIgnoreCase(sortBy)) {
            q.last("ORDER BY CONVERT(name USING gbk) ASC, id ASC");
        } else {
            // default 与 rank 同序：评级由高到低
            q.orderByDesc(SecretRealm::getRankScore).orderByAsc(SecretRealm::getId);
        }
        List<SecretRealm> realms = list(q);
        fillSignupInfo(realms, username);
        return realms;
    }

    @Override
    public List<OptionVO> grades() {
        Map<String, Integer> counts = toCountMap(baseMapper.countByGrade());
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        List<OptionVO> options = new ArrayList<>();
        options.add(new OptionVO(ALL, ALL, total));
        for (String g : GRADE_ORDER) {
            int n = counts.getOrDefault(g, 0);
            if (n > 0) {
                options.add(new OptionVO(g, g, n));
            }
        }
        return options;
    }

    @Override
    public List<String> hotWords() {
        List<String> hot = baseMapper.selectHotNames();
        if (hot != null && !hot.isEmpty()) {
            return hot;
        }
        return list(Wrappers.<SecretRealm>lambdaQuery()
                .orderByDesc(SecretRealm::getRankScore)
                .orderByAsc(SecretRealm::getId)
                .last("LIMIT 6"))
                .stream().map(SecretRealm::getName).toList();
    }

    @Override
    public SecretRealm detail(Long realmId, String username) {
        SecretRealm realm = getById(realmId);
        if (realm == null) {
            return null;
        }
        fillSignupInfo(List.of(realm), username);
        return realm;
    }

    @Override
    @Transactional
    public int signup(Long realmId, String username) {
        SecretRealm realm = getById(realmId);
        if (realm == null) {
            throw new IllegalArgumentException("未找到该秘境");
        }
        LambdaQueryWrapper<RealmSignup> dup = Wrappers.lambdaQuery();
        dup.eq(RealmSignup::getRealmId, realmId).eq(RealmSignup::getUsername, username);
        if (signupMapper.selectCount(dup) > 0) {
            throw new IllegalArgumentException("你已报名过该秘境，无需重复报名");
        }

        // 名额校验（quota 为 0 或空表示不限）
        Integer quota = realm.getQuota();
        if (quota != null && quota > 0) {
            long cnt = signupMapper.selectCount(Wrappers.<RealmSignup>lambdaQuery()
                    .eq(RealmSignup::getRealmId, realmId));
            if (cnt >= quota) {
                throw new IllegalArgumentException("「" + realm.getName() + "」本期名额已满，请下期赶早");
            }
        }

        RealmSignup signup = new RealmSignup();
        signup.setRealmId(realmId);
        signup.setUsername(username);
        signup.setSignedAt(LocalDateTime.now());
        signupMapper.insert(signup);

        return Math.toIntExact(signupMapper.selectCount(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getRealmId, realmId)));
    }

    @Override
    @Transactional
    public void cancelSignup(Long realmId, String username) {
        signupMapper.delete(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getRealmId, realmId)
                .eq(RealmSignup::getUsername, username));
    }

    @Override
    public List<SecretRealm> myRealms(String username) {
        List<RealmSignup> mine = signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getUsername, username)
                .orderByDesc(RealmSignup::getSignedAt));
        if (mine.isEmpty()) {
            return List.of();
        }
        List<Long> ids = mine.stream().map(RealmSignup::getRealmId).toList();
        List<SecretRealm> realms = listByIds(ids);
        fillSignupInfo(realms, username);
        // 按报名先后逆序展示（后报的在前）
        Map<Long, SecretRealm> map = new HashMap<>();
        realms.forEach(r -> map.put(r.getId(), r));
        return ids.stream().map(map::get).filter(java.util.Objects::nonNull).toList();
    }

    /** 填充每个秘境的报名人数与当前用户报名状态 */
    private void fillSignupInfo(List<SecretRealm> realms, String username) {
        if (realms == null || realms.isEmpty()) {
            return;
        }
        Map<Long, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : signupMapper.countByRealm()) {
            Object rid = row.get("realmId");
            Object cnt = row.get("cnt");
            if (rid != null) {
                counts.put(Long.valueOf(String.valueOf(rid)),
                        cnt instanceof Number ? ((Number) cnt).intValue() : 0);
            }
        }
        java.util.Set<Long> mine = new java.util.HashSet<>();
        if (username != null && !username.isBlank()) {
            signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                            .eq(RealmSignup::getUsername, username))
                    .forEach(s -> mine.add(s.getRealmId()));
        }
        for (SecretRealm r : realms) {
            r.setSignedCount(counts.getOrDefault(r.getId(), 0));
            r.setSigned(mine.contains(r.getId()));
        }
    }

    /** 「全部」/ 空 视为不筛选 */
    private boolean isActive(String v) {
        return v != null && !v.isBlank() && !ALL.equals(v.trim()) && !"all".equalsIgnoreCase(v.trim());
    }

    /** 把 [{name, cnt}] 聚合成有序 Map */
    private Map<String, Integer> toCountMap(List<Map<String, Object>> rows) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object name = row.get("name");
            Object cnt = row.get("cnt");
            if (name == null) {
                continue;
            }
            map.put(String.valueOf(name), cnt instanceof Number ? ((Number) cnt).intValue() : 0);
        }
        return map;
    }
}
