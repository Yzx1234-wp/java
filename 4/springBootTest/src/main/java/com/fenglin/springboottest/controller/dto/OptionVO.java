package com.fenglin.springboottest.controller.dto;

import lombok.Data;

/**
 * 筛选项（分类 / 品阶分级），带该选项下的典籍数量。
 */
@Data
public class OptionVO {

    /** 选项值（「全部」用 all 表示） */
    private String key;

    /** 选项显示名 */
    private String label;

    /** 该选项下的典籍数量 */
    private int count;

    public OptionVO() {
    }

    public OptionVO(String key, String label, int count) {
        this.key = key;
        this.label = label;
        this.count = count;
    }
}
