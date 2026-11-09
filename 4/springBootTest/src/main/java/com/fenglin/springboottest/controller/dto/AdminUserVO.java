package com.fenglin.springboottest.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户列表项（不含密码）。
 */
@Data
public class AdminUserVO {

    private Long id;

    private String username;

    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 总积分（已确认完成的任务积分 + 已确认完成的秘境积分） */
    private int points;

    /** 接取任务数 */
    private int questCount;

    /** 报名秘境数 */
    private int realmCount;

    /** 是否为后台管理员账号 */
    private boolean admin;
}
