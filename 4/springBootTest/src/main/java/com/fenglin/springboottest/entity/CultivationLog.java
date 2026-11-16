package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修炼记录：参悟 / 吐纳 / 突破 / 失败 留痕，「修炼境界」页时间线的数据源。
 */
@Data
@TableName("cultivation_log")
public class CultivationLog {

    /** 参悟功法 */
    public static final String TYPE_COMPREHEND = "COMPREHEND";
    /** 打坐吐纳 */
    public static final String TYPE_MEDITATE = "MEDITATE";
    /** 突破成功 */
    public static final String TYPE_BREAKTHROUGH = "BREAKTHROUGH";
    /** 渡劫失败 */
    public static final String TYPE_FAIL = "FAIL";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号 */
    private String username;

    /** 类型：COMPREHEND / MEDITATE / BREAKTHROUGH / FAIL */
    private String type;

    /** 标题 */
    private String title;

    /** 细节说明 */
    private String detail;

    /** 修为变化（可为负） */
    private Integer expDelta;

    /** 发生时的境界序号 */
    private Integer realmIndex;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
