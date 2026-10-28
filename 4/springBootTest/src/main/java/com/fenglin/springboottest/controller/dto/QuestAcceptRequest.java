package com.fenglin.springboottest.controller.dto;

import lombok.Data;

/**
 * 任务接取请求体：{"questId": 1}
 */
@Data
public class QuestAcceptRequest {

    /** 任务编号 */
    private Long questId;
}
