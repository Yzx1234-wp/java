package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.GongfaComprehend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface GongfaComprehendMapper extends BaseMapper<GongfaComprehend> {

    /** 各账号已参悟典籍数（后台修士境界列表用） */
    @Select("SELECT username AS username, COUNT(*) AS cnt FROM gongfa_comprehend GROUP BY username")
    List<Map<String, Object>> countByUser();

    /** 全站累计参悟次数（后台概览用） */
    @Select("SELECT COUNT(*) FROM gongfa_comprehend")
    int countAll();
}
