package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.QuestAccept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestAcceptMapper extends BaseMapper<QuestAccept> {

    /** 各任务接取人数与已确认完成人数 */
    @Select("SELECT quest_id AS questId, COUNT(*) AS cnt, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS done "
            + "FROM quest_accept GROUP BY quest_id")
    List<Map<String, Object>> countByQuest();

    /** 账号总积分（只累计后台已确认完成的记录） */
    @Select("SELECT COALESCE(SUM(points), 0) FROM quest_accept "
            + "WHERE username = #{username} AND status = 'COMPLETED'")
    int sumPoints(String username);

    /** 账号待完成记录数（尚未发放积分） */
    @Select("SELECT COUNT(*) FROM quest_accept WHERE username = #{username} AND status <> 'COMPLETED'")
    int countPending(String username);

    /** 按账号统计接取数量、已完成数量与已发放积分（后台用户列表用） */
    @Select("SELECT username AS username, COUNT(*) AS cnt, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS done, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN points ELSE 0 END), 0) AS pts "
            + "FROM quest_accept GROUP BY username")
    List<Map<String, Object>> statsByUser();

    /** 全部已发放积分（后台概览用） */
    @Select("SELECT COALESCE(SUM(points), 0) FROM quest_accept WHERE status = 'COMPLETED'")
    int sumAllPoints();

    /** 全站待完成接取数（后台概览用） */
    @Select("SELECT COUNT(*) FROM quest_accept WHERE status <> 'COMPLETED'")
    int countAllPending();

    /** 全站已完成接取数（后台概览用） */
    @Select("SELECT COUNT(*) FROM quest_accept WHERE status = 'COMPLETED'")
    int countAllCompleted();
}
