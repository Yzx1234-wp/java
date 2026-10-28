package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.QuestMapper;
import com.fenglin.springboottest.service.QuestService;
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
public class QuestServiceImpl extends ServiceImpl<QuestMapper, Quest> implements QuestService {

    /** 「全部」筛选项的 key / label，与前端保持一致 */
    private static final String ALL = "全部";

    /** 任务等级展示顺序（由高到低，等级越高积分越多） */
    private static final List<String> GRADE_ORDER = List.of("仙帝", "仙王", "真仙", "上古");

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Override
    public List<Quest> search(String keyword, String grade, String sortBy, String username) {
        LambdaQueryWrapper<Quest> q = Wrappers.lambdaQuery();

        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(Quest::getName, k)
                    .or().like(Quest::getGrade, k)
                    .or().like(Quest::getPoster, k)
                    .or().like(Quest::getDescription, k));
        }
        if (isActive(grade)) {
            q.eq(Quest::getGrade, grade);
        }

        if ("name".equalsIgnoreCase(sortBy)) {
            q.last("ORDER BY CONVERT(name USING gbk) ASC, id ASC");
        } else {
            // default 与 rank 同序：积分由高到低（等级越高越靠前）
            q.orderByDesc(Quest::getPoints).orderByAsc(Quest::getId);
        }
        List<Quest> quests = list(q);
        fillAcceptInfo(quests, username);
        return quests;
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
        return list(Wrappers.<Quest>lambdaQuery()
                .orderByDesc(Quest::getPoints)
                .orderByAsc(Quest::getId)
                .last("LIMIT 6"))
                .stream().map(Quest::getName).toList();
    }

    @Override
    public Quest detail(Long questId, String username) {
        Quest quest = getById(questId);
        if (quest == null) {
            return null;
        }
        fillAcceptInfo(List.of(quest), username);
        return quest;
    }

    @Override
    @Transactional
    public int accept(Long questId, String username) {
        Quest quest = getById(questId);
        if (quest == null) {
            throw new IllegalArgumentException("未找到该任务");
        }
        LambdaQueryWrapper<QuestAccept> dup = Wrappers.lambdaQuery();
        dup.eq(QuestAccept::getQuestId, questId).eq(QuestAccept::getUsername, username);
        if (acceptMapper.selectCount(dup) > 0) {
            throw new IllegalArgumentException("你已接取过「" + quest.getName() + "」，不可重复接取");
        }

        QuestAccept record = new QuestAccept();
        record.setQuestId(questId);
        record.setUsername(username);
        record.setPoints(quest.getPoints() == null ? 0 : quest.getPoints());
        record.setAcceptedAt(LocalDateTime.now());
        acceptMapper.insert(record);
        return record.getPoints();
    }

    @Override
    public List<Quest> myQuests(String username) {
        List<QuestAccept> mine = acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getUsername, username)
                .orderByDesc(QuestAccept::getAcceptedAt));
        if (mine.isEmpty()) {
            return List.of();
        }
        List<Long> ids = mine.stream().map(QuestAccept::getQuestId).toList();
        List<Quest> quests = listByIds(ids);
        fillAcceptInfo(quests, username);
        Map<Long, Quest> map = new HashMap<>();
        quests.forEach(q -> map.put(q.getId(), q));
        return ids.stream().map(map::get).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    public int myPoints(String username) {
        return acceptMapper.sumPoints(username);
    }

    /** 填充每个任务的接取人数与当前用户接取状态 */
    private void fillAcceptInfo(List<Quest> quests, String username) {
        if (quests == null || quests.isEmpty()) {
            return;
        }
        Map<Long, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : acceptMapper.countByQuest()) {
            Object qid = row.get("questId");
            Object cnt = row.get("cnt");
            if (qid != null) {
                counts.put(Long.valueOf(String.valueOf(qid)),
                        cnt instanceof Number ? ((Number) cnt).intValue() : 0);
            }
        }
        java.util.Set<Long> mine = new java.util.HashSet<>();
        if (username != null && !username.isBlank()) {
            acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                            .eq(QuestAccept::getUsername, username))
                    .forEach(a -> mine.add(a.getQuestId()));
        }
        for (Quest q : quests) {
            q.setAcceptedCount(counts.getOrDefault(q.getId(), 0));
            q.setAccepted(mine.contains(q.getId()));
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
