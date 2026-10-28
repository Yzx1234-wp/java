package com.fenglin.springboottest.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 「我的主页」聚合 VO：账号信息 + 总积分 + 境界进度 + 成就徽章 + 修炼动态。
 */
@Data
public class ProfileVO {

    /** 账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 注册时间（加入大荒之日） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 总积分（接取任务累积） */
    private Integer totalPoints;

    /** 已接任务数 */
    private Integer questCount;

    /** 已报名秘境数 */
    private Integer realmCount;

    /** 已点亮成就数 */
    private Integer badgeCount;

    /** 当前修炼境界（按总积分划定） */
    private Tier tier;

    /** 成就徽章（含未点亮的） */
    private List<Badge> badges;

    /** 修炼动态（任务接取 + 秘境报名合并时间线） */
    private List<Activity> activities;

    @Data
    public static class Tier {
        /** 当前境界名，如「尊者」 */
        private String name;
        /** 境界序号（从 0 起） */
        private Integer index;
        /** 距下一境进度（0-100，已是最高境则为 100） */
        private Double progress;
        /** 下一境界名（已是最高境则为 null） */
        private String next;
        /** 晋升下一境界所需总积分 */
        private Integer nextPoints;
    }

    @Data
    public static class Badge {
        private String key;
        private String name;
        private String desc;
        private String icon;
        private Boolean unlocked;
    }

    @Data
    public static class Activity {
        /** 类型：quest 任务 / realm 秘境 */
        private String type;
        /** 名称（任务名或秘境名） */
        private String name;
        /** 图标 */
        private String glyph;
        /** 等级 */
        private String grade;
        /** 任务动态才有：获得积分 */
        private Integer points;
        /** 发生时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;
    }
}
