package com.fenglin.springboottest.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;

    /** 权限：0 管理员 / 1 默认（普通修士）；前端据此显隐「后台管理」入口 */
    private Integer permission;
}
