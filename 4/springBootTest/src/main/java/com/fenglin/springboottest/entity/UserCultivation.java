package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修士修炼档案：当前境界、本境已积修为、突破记录。
 * <p>境界不随积分自动上涨，只有修为积满并渡劫成功才会推进 realmIndex。</p>
 */
@Data
@TableName("user_cultivation")
public class UserCultivation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号 */
    private String username;

    /** 当前境界序号（0=搬血 … 17=仙帝） */
    private Integer realmIndex;

    /** 本境已积修为（突破成功后结转溢出部分） */
    private Integer exp;

    /** 历史累计修为（只增不减） */
    private Integer totalExp;

    /** 成功突破次数 */
    private Integer breakthroughCount;

    /** 渡劫失败次数 */
    private Integer failCount;

    /** 上次打坐吐纳时间（冷却判定） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMeditateAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
