package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.Quest;

import java.util.List;

/**
 * 大荒悬赏任务业务接口。
 */
public interface QuestService extends IService<Quest> {

    /**
     * 任务检索：关键词（名称 / 等级 / 发布方 / 简介模糊匹配），
     * 可按等级筛选，支持默认（积分递降）、按积分、按名称三种排序。
     *
     * @param username 当前登录账号，可为空（用于标记「是否已接取」）
     */
    List<Quest> search(String keyword, String grade, String sortBy, String username);

    /** 等级筛选项（含数量，「全部」在首位） */
    List<OptionVO> grades();

    /** 热门任务名称 */
    List<String> hotWords();

    /** 任务详情（填充接取人数与当前用户接取状态） */
    Quest detail(Long questId, String username);

    /**
     * 接取任务（每人每项限接一次，接取即发积分）。
     *
     * @return 本次获得的积分
     */
    int accept(Long questId, String username);

    /** 我的接取列表 */
    List<Quest> myQuests(String username);

    /** 我的总积分 */
    int myPoints(String username);
}
