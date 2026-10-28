package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.SecretRealm;

import java.util.List;

/**
 * 大荒秘境业务接口。
 */
public interface RealmService extends IService<SecretRealm> {

    /**
     * 秘境检索：关键词（名称 / 等级 / 所在地 / 开启时间 / 介绍模糊匹配），
     * 可按等级筛选，支持默认、按评级、按名称三种排序。
     *
     * @param username 当前登录账号，可为空（用于标记「是否已报名」）
     */
    List<SecretRealm> search(String keyword, String grade, String sortBy, String username);

    /** 等级筛选项（含数量，「全部」在首位） */
    List<OptionVO> grades();

    /** 热门秘境名称 */
    List<String> hotWords();

    /** 秘境详情（填充报名人数与当前用户报名状态） */
    SecretRealm detail(Long realmId, String username);

    /**
     * 报名秘境（每人每期限报一次，名额已满则拒绝）。
     *
     * @return 报名成功后的最新报名人数
     */
    int signup(Long realmId, String username);

    /** 取消报名 */
    void cancelSignup(Long realmId, String username);

    /** 我的报名列表 */
    List<SecretRealm> myRealms(String username);
}
