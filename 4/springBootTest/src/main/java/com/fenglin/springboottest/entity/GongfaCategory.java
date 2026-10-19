package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 功法分类字典（首页「类别」筛选的数据源）。
 */
@Data
@TableName("gongfa_category")
public class GongfaCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类编码 */
    private String code;

    /** 分类名称 */
    private String name;

    /** 排序号 */
    private Integer sortNo;

    /** 分类说明 */
    private String remark;
}
