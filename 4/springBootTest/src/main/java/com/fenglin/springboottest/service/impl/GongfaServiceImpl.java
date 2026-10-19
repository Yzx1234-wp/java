package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.controller.dto.HomeVO;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.Gongfa;
import com.fenglin.springboottest.entity.GongfaCategory;
import com.fenglin.springboottest.mapper.GongfaCategoryMapper;
import com.fenglin.springboottest.mapper.GongfaMapper;
import com.fenglin.springboottest.service.GongfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GongfaServiceImpl extends ServiceImpl<GongfaMapper, Gongfa> implements GongfaService {

    /** 「全部」筛选项的 key / label，与前端保持一致 */
    private static final String ALL = "全部";

    /** 品阶分级展示顺序（由高到低） */
    private static final List<String> GRADE_ORDER = List.of("绝世", "绝顶", "一流", "上乘", "中乘");

    @Autowired
    private GongfaCategoryMapper categoryMapper;

    @Override
    public List<Gongfa> search(String keyword, String cat, String grade, String sortBy) {
        LambdaQueryWrapper<Gongfa> q = Wrappers.lambdaQuery();

        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            // 关键词在下列字段中做「或」匹配，实现全库模糊检索
            q.and(w -> w.like(Gongfa::getName, k)
                    .or().like(Gongfa::getCat, k)
                    .or().like(Gongfa::getTier, k)
                    .or().like(Gongfa::getOrigin, k)
                    .or().like(Gongfa::getOwner, k)
                    .or().like(Gongfa::getGrade, k)
                    .or().like(Gongfa::getDescription, k));
        }
        if (isActive(cat)) {
            q.eq(Gongfa::getCat, cat);
        }
        if (isActive(grade)) {
            q.eq(Gongfa::getGrade, grade);
        }

        if ("name".equalsIgnoreCase(sortBy)) {
            // 中文按拼音序：借助 GBK 编码顺序（GBK 一级汉字即按拼音排列）
            q.last("ORDER BY CONVERT(name USING gbk) ASC, id ASC");
        } else {
            // default 与 rank 同序：品阶由高到低，同品阶按入库顺序
            q.orderByDesc(Gongfa::getRankScore).orderByAsc(Gongfa::getId);
        }
        return list(q);
    }

    @Override
    public List<OptionVO> categories() {
        Map<String, Integer> counts = byCat();
        List<GongfaCategory> dict = categoryMapper.selectList(
                Wrappers.<GongfaCategory>lambdaQuery().orderByAsc(GongfaCategory::getSortNo));

        List<OptionVO> options = new ArrayList<>();
        options.add(new OptionVO(ALL, ALL, counts.values().stream().mapToInt(Integer::intValue).sum()));
        for (GongfaCategory c : dict) {
            int n = counts.getOrDefault(c.getCode(), 0);
            if (n > 0) {
                options.add(new OptionVO(c.getCode(), c.getName(), n));
            }
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
    public Map<String, Integer> byCat() {
        return toCountMap(baseMapper.countByCat());
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
        // 未标记热门时，退化为品阶最高的前 6 部
        return list(Wrappers.<Gongfa>lambdaQuery()
                .orderByDesc(Gongfa::getRankScore)
                .orderByAsc(Gongfa::getId)
                .last("LIMIT 6"))
                .stream().map(Gongfa::getName).toList();
    }

    @Override
    public HomeVO home() {
        HomeVO vo = new HomeVO();
        vo.setTotal((int) count());
        vo.setByCat(byCat());
        vo.setByGrade(byGrade());
        vo.setCategories(categories());
        vo.setGrades(grades());
        vo.setHotWords(hotWords());
        vo.setList(search(null, null, null, "default"));
        return vo;
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
