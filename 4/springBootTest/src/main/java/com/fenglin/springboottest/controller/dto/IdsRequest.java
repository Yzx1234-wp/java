package com.fenglin.springboottest.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量操作请求体：{ "ids": [1,2,3] }
 */
@Data
public class IdsRequest {

    private List<Long> ids;
}
