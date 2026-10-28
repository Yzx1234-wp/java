package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.WorldCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorldCharacterMapper extends BaseMapper<WorldCharacter> {

    /** 按出身势力统计数量 */
    @Select("SELECT faction AS name, COUNT(*) AS cnt FROM world_character GROUP BY faction")
    List<Map<String, Object>> countByFaction();

    /** 按定位分级统计数量 */
    @Select("SELECT grade AS name, COUNT(*) AS cnt FROM world_character GROUP BY grade")
    List<Map<String, Object>> countByGrade();

    /** 热门人物姓名（按实力递降取前 6 位） */
    @Select("SELECT name FROM world_character WHERE is_hot = 1 ORDER BY rank_score DESC, id ASC LIMIT 6")
    List<String> selectHotNames();
}
