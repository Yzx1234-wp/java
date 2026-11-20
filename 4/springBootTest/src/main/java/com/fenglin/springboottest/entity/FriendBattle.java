package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 好友对战（象棋/五子棋，自定义积分注；招法存 moves JSON 数组） */
@Data
@TableName("friend_battle")
public class FriendBattle {

    /** 对局项目：象棋 */
    public static final String GAME_CHESS = "CHESS";
    /** 对局项目：五子棋 */
    public static final String GAME_GOMOKU = "GOMOKU";

    /** 状态：待对方应战 */
    public static final String STATUS_REQUESTED = "REQUESTED";
    /** 状态：对战中 */
    public static final String STATUS_READY = "READY";
    /** 状态：已结算 */
    public static final String STATUS_FINISHED = "FINISHED";
    /** 状态：已取消 */
    public static final String STATUS_CANCELLED = "CANCELLED";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起人账号（执黑/执红，先行） */
    private String challenger;

    /** 应战人账号 */
    private String opponent;

    /** 对局项目：CHESS 象棋 / GOMOKU 五子棋 */
    private String game;

    /** 自定义积分注（胜方净得 stake，负方扣除 stake） */
    private Integer stake;

    /** 状态：REQUESTED 待应战 / READY 对战中 / FINISHED 已结算 / CANCELLED 已取消 */
    private String status;

    /** 胜方账号（结算后回填） */
    private String winner;

    /** 招法 JSON 数组（如 ["7,7","7,8"]；象棋为 "r1,c1,r2,c2"，五子棋为 "r,c"） */
    private String moves;

    /** 发起时间 */
    private LocalDateTime createdAt;

    /** 结算时间 */
    private LocalDateTime finishedAt;
}
