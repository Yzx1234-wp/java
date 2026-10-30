package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.WorldCharacter;

import java.util.List;
import java.util.Map;

/**
 * 大荒人物志业务接口。
 */
public interface CharacterService extends IService<WorldCharacter> {

    /**
     * 人物检索：关键词（姓名 / 称号 / 境界 / 势力 / 语录 / 简介模糊匹配），
     * 并可按出身势力、定位分级筛选，支持默认、按实力、按姓名三种排序。
     *
     * @param keyword 关键词，可为空
     * @param faction 出身势力，「全部」或空表示不筛选
     * @param grade   定位分级，「全部」或空表示不筛选
     * @param sortBy  default / rank / name
     */
    List<WorldCharacter> search(String keyword, String faction, String grade, String sortBy);

    /** 势力筛选项（含数量，「全部」在首位） */
    List<OptionVO> factions();

    /** 定位分级筛选项（含数量，「全部」在首位） */
    List<OptionVO> grades();

    /** 各势力数量 */
    Map<String, Integer> byFaction();

    /** 各定位分级数量 */
    Map<String, Integer> byGrade();

    /** 热门人物姓名 */
    List<String> hotWords();

    /**
     * 实力排行榜（天骄榜）：按实力评分由高到低取前 N 位。
     *
     * @param limit 榜单长度（1~50，越界自动收敛）
     */
    List<WorldCharacter> ranking(int limit);
}
