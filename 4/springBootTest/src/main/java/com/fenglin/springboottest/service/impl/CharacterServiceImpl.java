package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.WorldCharacter;
import com.fenglin.springboottest.mapper.WorldCharacterMapper;
import com.fenglin.springboottest.service.CharacterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CharacterServiceImpl extends ServiceImpl<WorldCharacterMapper, WorldCharacter> implements CharacterService {

    /** 「全部」筛选项的 key / label，与前端保持一致 */
    private static final String ALL = "全部";

    /** 定位分级展示顺序（由高到低） */
    private static final List<String> GRADE_ORDER = List.of("仙帝", "准仙帝", "仙王", "真仙", "尊者");

    @Override
    public List<WorldCharacter> search(String keyword, String faction, String grade, String sortBy) {
        LambdaQueryWrapper<WorldCharacter> q = Wrappers.lambdaQuery();

        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            // 关键词在下列字段中做「或」匹配，实现全库模糊检索
            q.and(w -> w.like(WorldCharacter::getName, k)
                    .or().like(WorldCharacter::getTitle, k)
                    .or().like(WorldCharacter::getRealm, k)
                    .or().like(WorldCharacter::getFaction, k)
                    .or().like(WorldCharacter::getGrade, k)
                    .or().like(WorldCharacter::getQuote, k)
                    .or().like(WorldCharacter::getDescription, k));
        }
        if (isActive(faction)) {
            q.eq(WorldCharacter::getFaction, faction);
        }
        if (isActive(grade)) {
            q.eq(WorldCharacter::getGrade, grade);
        }

        if ("name".equalsIgnoreCase(sortBy)) {
            // 中文按拼音序：借助 GBK 编码顺序（GBK 一级汉字即按拼音排列）
            q.last("ORDER BY CONVERT(name USING gbk) ASC, id ASC");
        } else {
            // default 与 rank 同序：实力由高到低，同实力按入库顺序
            q.orderByDesc(WorldCharacter::getRankScore).orderByAsc(WorldCharacter::getId);
        }
        return list(q);
    }

    @Override
    public List<OptionVO> factions() {
        Map<String, Integer> counts = byFaction();
        List<OptionVO> options = new ArrayList<>();
        options.add(new OptionVO(ALL, ALL, counts.values().stream().mapToInt(Integer::intValue).sum()));
        // 按该势力最强人物排序（Map 本身按首次出现顺序，即实力递降）
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            options.add(new OptionVO(e.getKey(), e.getKey(), e.getValue()));
        }
        return options;
    }

    @Override
    public List<OptionVO> grades() {
        Map<String, Integer> counts = byGrade();
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
    public Map<String, Integer> byFaction() {
        return toCountMap(baseMapper.countByFaction());
    }

    @Override
    public Map<String, Integer> byGrade() {
        return toCountMap(baseMapper.countByGrade());
    }

    @Override
    public List<String> hotWords() {
        List<String> hot = baseMapper.selectHotNames();
        if (hot != null && !hot.isEmpty()) {
            return hot;
        }
        // 未标记热门时，退化为实力最强的前 6 位
        return list(Wrappers.<WorldCharacter>lambdaQuery()
                .orderByDesc(WorldCharacter::getRankScore)
                .orderByAsc(WorldCharacter::getId)
                .last("LIMIT 6"))
                .stream().map(WorldCharacter::getName).toList();
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
            int n = cnt instanceof Number ? ((Number) cnt).intValue() : 0;
            map.put(String.valueOf(name), n);
        }
        return map;
    }
}
