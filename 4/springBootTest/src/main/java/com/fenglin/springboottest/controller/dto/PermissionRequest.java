package com.fenglin.springboottest.controller.dto;

import lombok.Data;

/** 权限切换请求体：{"permission": 0 或 1} */
@Data
public class PermissionRequest {

    /** 0 管理员 / 1 默认（普通修士） */
    private Integer permission;
}
