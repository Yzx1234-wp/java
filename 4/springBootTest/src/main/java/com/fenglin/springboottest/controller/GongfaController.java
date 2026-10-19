package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.HomeVO;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.Gongfa;
import com.fenglin.springboottest.service.GongfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页「大荒功法库」接口。
 * 首页为公开可读数据，不强制登录态。
 */
@RestController
@RequestMapping("/api/gongfa")
public class GongfaController {

    @Autowired
    private GongfaService gongfaService;

    /** 首页聚合：统计 + 分类/分级筛选项 + 热门典籍 + 默认列表（一屏数据一次拿全） */
    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.ok(gongfaService.home());
    }

    /**
     * 功法检索（分级分类搜索）
     *
     * @param keyword 关键词，模糊匹配 名称/分类/品阶/传承/修习者/简介
     * @param cat     一级分类：功法 / 宝术 / 至尊术 / 剑诀 / 秘法 / 身法，「全部」或空为不筛
     * @param grade   品阶分级：绝世 / 绝顶 / 一流 / 上乘 / 中乘，「全部」或空为不筛
     * @param sortBy  排序：default（默认，品阶递降）/ rank（按品阶）/ name（按名称）
     */
    @GetMapping("/list")
    public Result<List<Gongfa>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy) {
        return Result.ok(gongfaService.search(keyword, cat, grade, sortBy));
    }

    /** 分类筛选项（含数量） */
    @GetMapping("/categories")
    public Result<List<OptionVO>> categories() {
        return Result.ok(gongfaService.categories());
    }

    /** 品阶分级筛选项（含数量） */
    @GetMapping("/grades")
    public Result<List<OptionVO>> grades() {
        return Result.ok(gongfaService.grades());
    }

    /** 热门典籍关键词 */
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.ok(gongfaService.hotWords());
    }

    /** 典籍详情 */
    @GetMapping("/{id}")
    public Result<Gongfa> detail(@PathVariable Long id) {
        Gongfa g = gongfaService.getById(id);
        if (g == null) {
            return Result.fail("未找到该功法典籍");
        }
        return Result.ok(g);
    }
}
