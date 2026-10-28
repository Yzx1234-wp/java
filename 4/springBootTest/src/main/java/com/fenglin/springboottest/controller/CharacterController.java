package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.WorldCharacter;
import com.fenglin.springboottest.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 「大荒人物志」人物介绍接口。
 * 人物数据为公开演示数据，不强制登录态（与功法库保持一致）。
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    /**
     * 人物检索（分级分类搜索）
     *
     * @param keyword 关键词，模糊匹配 姓名/称号/境界/势力/语录/简介
     * @param faction 出身势力，「全部」或空为不筛
     * @param grade   定位分级：仙帝 / 准仙帝 / 仙王 / 真仙 / 尊者，「全部」或空为不筛
     * @param sortBy  排序：default（默认，实力递降）/ rank（按实力）/ name（按姓名）
     */
    @GetMapping("/list")
    public Result<List<WorldCharacter>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String faction,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy) {
        return Result.ok(characterService.search(keyword, faction, grade, sortBy));
    }

    /** 势力筛选项（含数量） */
    @GetMapping("/factions")
    public Result<List<OptionVO>> factions() {
        return Result.ok(characterService.factions());
    }

    /** 定位分级筛选项（含数量） */
    @GetMapping("/grades")
    public Result<List<OptionVO>> grades() {
        return Result.ok(characterService.grades());
    }

    /** 热门人物姓名 */
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.ok(characterService.hotWords());
    }

    /** 人物详情 */
    @GetMapping("/{id}")
    public Result<WorldCharacter> detail(@PathVariable Long id) {
        WorldCharacter c = characterService.getById(id);
        if (c == null) {
            return Result.fail("未找到该人物");
        }
        return Result.ok(c);
    }
}
