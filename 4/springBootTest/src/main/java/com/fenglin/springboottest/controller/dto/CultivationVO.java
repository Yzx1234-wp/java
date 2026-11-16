package com.fenglin.springboottest.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * 「修炼境界」页总览 VO：当前境界 + 修为进度 + 渡劫成功率 + 十八境阶梯 + 可参悟典籍。
 */
@Data
public class CultivationVO {

    /** 账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 当前境界序号（0 起） */
    private Integer tierIndex;

    /** 当前境界名，如「铭纹」 */
    private String tierName;

    /** 当前境界说明 */
    private String tierTip;

    /** 本境已积修为 */
    private Integer exp;

    /** 冲击下一境所需修为（最高境为 0） */
    private Integer need;

    /** 本境修为进度（0-100，最高境恒为 100） */
    private Double progress;

    /** 距突破还需修为 */
    private Integer remain;

    /** 修为是否已够，可以冲击境界 */
    private Boolean canBreak;

    /** 是否已至最高境（仙帝） */
    private Boolean top;

    /** 下一境界名（最高境为 null） */
    private String next;

    /** 渡劫成功率（%） */
    private Integer successRate;

    /** 已参悟功法数 */
    private Integer comprehendCount;

    /** 功法总数（可参悟上限） */
    private Integer comprehendTotal;

    /** 成功突破次数 */
    private Integer breakthroughCount;

    /** 渡劫失败次数 */
    private Integer failCount;

    /** 历史累计修为 */
    private Integer totalExp;

    /** 打坐吐纳一次可得修为 */
    private Integer meditateGain;

    /** 打坐吐纳冷却剩余秒数（0 表示可以立即吐纳） */
    private Integer meditateCooldown;

    /** 打坐吐纳冷却总秒数 */
    private Integer meditateCooldownTotal;

    /** 十八境阶梯（含状态：done 已达成 / current 当前 / locked 未至） */
    private List<StepView> ladder;

    /** 功法参悟列表（按品阶递降） */
    private List<ComprehendItem> gongfa;

    @Data
    public static class StepView {
        private Integer index;
        private String name;
        private Integer need;
        private String tip;
        /** done / current / locked */
        private String state;
    }

    @Data
    public static class ComprehendItem {
        private Long id;
        private String name;
        private String cat;
        private String grade;
        private String glyph;
        private String tier;
        /** 品阶评分 */
        private Double rank;
        /** 参悟可得修为 */
        private Integer gain;
        /** 是否已参悟（已参悟不可重复参悟） */
        private Boolean comprehended;
    }
}
