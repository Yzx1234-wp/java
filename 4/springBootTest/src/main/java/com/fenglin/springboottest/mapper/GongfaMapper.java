package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Gongfa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface GongfaMapper extends BaseMapper<Gongfa> {

    /** 按一级分类统计数量 */
    @Select("SELECT cat AS name, COUNT(*) AS cnt FROM gongfa GROUP BY cat")
    List<Map<String, Object>> countByCat();

    /** 按品阶分级统计数量 */
    @Select("SELECT grade AS name, COUNT(*) AS cnt FROM gongfa GROUP BY grade")
    List<Map<String, Object>> countByGrade();

    /** 热门典籍名称（按品阶递降取前 6 部） */
    @Select("SELECT name FROM gongfa WHERE is_hot = 1 ORDER BY rank_score DESC, id ASC LIMIT 6")
    List<String> selectHotNames();
}
