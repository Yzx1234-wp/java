package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.controller.dto.HomeVO;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.Gongfa;

import java.util.List;
import java.util.Map;

/**
 * 大荒功法库业务接口。
 */
public interface GongfaService extends IService<Gongfa> {

    /**
     * 功法检索：关键词（名称 / 分类 / 品阶 / 传承 / 修习者 / 简介模糊匹配），
     * 并可按一级分类、品阶分级筛选，支持默认、按品阶、按名称三种排序。
     *
     * @param keyword 关键词，可为空
     * @param cat     一级分类，「全部」或空表示不筛选
     * @param grade   品阶分级，「全部」或空表示不筛选
     * @param sortBy  default / rank / name
     */
    List<Gongfa> search(String keyword, String cat, String grade, String sortBy);

    /** 分类筛选项（含数量，「全部」在首位） */
    List<OptionVO> categories();

    /** 品阶分级筛选项（含数量，「全部」在首位） */
    List<OptionVO> grades();

    /** 各一级分类数量 */
    Map<String, Integer> byCat();

    /** 各品阶分级数量 */
    Map<String, Integer> byGrade();

    /** 热门典籍名称 */
    List<String> hotWords();

    /** 首页聚合数据 */
    HomeVO home();
}
