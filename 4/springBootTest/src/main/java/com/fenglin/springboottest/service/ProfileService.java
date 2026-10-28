package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.controller.dto.ProfileVO;
import com.fenglin.springboottest.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 「我的主页」聚合服务：积分 / 已接任务 / 已报秘境 / 境界 / 成就 / 动态。
 */
public interface ProfileService extends IService<User> {

    /** 个人主页总览（账号信息 + 积分 + 境界 + 成就 + 动态） */
    ProfileVO summary(String username);

    /** 我接取的任务明细（含接取时间与留档积分） */
    List<Map<String, Object>> myQuests(String username);

    /** 我报名的秘境明细（含报名时间与开启时间） */
    List<Map<String, Object>> myRealms(String username);
}
