package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.QuestAccept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestAcceptMapper extends BaseMapper<QuestAccept> {

    /** 各任务接取人数 */
    @Select("SELECT quest_id AS questId, COUNT(*) AS cnt FROM quest_accept GROUP BY quest_id")
    List<Map<String, Object>> countByQuest();

    /** 账号总积分（按接取留档积分求和） */
    @Select("SELECT COALESCE(SUM(points), 0) FROM quest_accept WHERE username = #{username}")
    int sumPoints(String username);

    /** 按账号统计接取数量与总积分（后台用户列表用） */
    @Select("SELECT username AS username, COUNT(*) AS cnt, COALESCE(SUM(points), 0) AS pts "
            + "FROM quest_accept GROUP BY username")
    List<Map<String, Object>> statsByUser();
}
