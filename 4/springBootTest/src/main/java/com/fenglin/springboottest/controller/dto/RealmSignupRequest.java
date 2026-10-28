package com.fenglin.springboottest.controller.dto;

import lombok.Data;

/**
 * 秘境报名请求体：{"realmId": 1}
 */
@Data
public class RealmSignupRequest {

    /** 秘境编号 */
    private Long realmId;
}
