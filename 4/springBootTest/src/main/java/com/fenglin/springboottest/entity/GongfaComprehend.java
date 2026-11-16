package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 功法参悟记录：每部典籍每人仅可参悟一次，是修为的主要来源。
 */
@Data
@TableName("gongfa_comprehend")
public class GongfaComprehend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号 */
    private String username;

    /** 功法 id */
    private Long gongfaId;

    /** 本次参悟所得修为 */
    private Integer expGain;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime comprehendedAt;
}
