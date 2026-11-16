package com.fenglin.springboottest.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台「修士境界」列表 VO：账号 + 境界 + 修为与参悟统计。
 */
@Data
public class AdminCultivationVO {

    /** 账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 是否管理员 */
    private Boolean admin;

    /** 是否已入门（存在修炼档案） */
    private Boolean profiled;

    /** 当前境界序号（0 起；未入门按 0 展示） */
    private Integer realmIndex;

    /** 当前境界名 */
    private String realmName;

    /** 本境已积修为 */
    private Integer exp;

    /** 冲击下一境所需修为（仙帝为 0） */
    private Integer need;

    /** 本境修为进度（0-100） */
    private Double progress;

    /** 历史累计修为 */
    private Integer totalExp;

    /** 已参悟典籍数 */
    private Integer comprehendCount;

    /** 成功破境次数 */
    private Integer breakthroughCount;

    /** 渡劫失败次数 */
    private Integer failCount;

    /** 最近一次打坐吐纳 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMeditateAt;

    /** 档案最近更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
