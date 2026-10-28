package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Quest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestMapper extends BaseMapper<Quest> {

    /** 按任务等级统计数量 */
    @Select("SELECT grade AS name, COUNT(*) AS cnt FROM quest GROUP BY grade")
    List<Map<String, Object>> countByGrade();

    /** 热门任务名称（按积分递降取前 6 个） */
    @Select("SELECT name FROM quest WHERE is_hot = 1 ORDER BY points DESC, id ASC LIMIT 6")
    List<String> selectHotNames();
}
