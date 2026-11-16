package com.fenglin.springboottest.controller.dto;

import lombok.Data;

/** 商店兑换请求体：{"itemId": 1} */
@Data
public class ShopRedeemRequest {

    private Long itemId;
}
