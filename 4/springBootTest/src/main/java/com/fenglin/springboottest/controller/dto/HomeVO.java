package com.fenglin.springboottest.controller.dto;

import com.fenglin.springboottest.entity.Gongfa;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 首页一次性聚合数据：统计 + 分类/分级筛选项 + 热门典籍 + 默认列表。
 */
@Data
public class HomeVO {

    /** 典籍总数 */
    private int total;

    /** 各一级分类数量 */
    private Map<String, Integer> byCat;

    /** 各品阶分级数量 */
    private Map<String, Integer> byGrade;

    /** 分类筛选项（含「全部」） */
    private List<OptionVO> categories;

    /** 品阶分级筛选项（含「全部」） */
    private List<OptionVO> grades;

    /** 热门典籍关键词 */
    private List<String> hotWords;

    /** 默认列表（按品阶递降） */
    private List<Gongfa> list;
}
