package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.RealmSignup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RealmSignupMapper extends BaseMapper<RealmSignup> {

    /** 各秘境报名人数与已确认完成人数 */
    @Select("SELECT realm_id AS realmId, COUNT(*) AS cnt, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS done "
            + "FROM realm_signup GROUP BY realm_id")
    List<Map<String, Object>> countByRealm();

    /** 按账号统计报名数量、已完成数量与已发放积分（后台用户列表用） */
    @Select("SELECT username AS username, COUNT(*) AS cnt, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS done, "
            + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN points ELSE 0 END), 0) AS pts "
            + "FROM realm_signup GROUP BY username")
    List<Map<String, Object>> countByUser();

    /** 账号秘境已完成积分（只累计后台已确认完成的记录） */
    @Select("SELECT COALESCE(SUM(points), 0) FROM realm_signup "
            + "WHERE username = #{username} AND status = 'COMPLETED'")
    int sumPoints(String username);

    /** 账号待完成报名数（尚未发放积分） */
    @Select("SELECT COUNT(*) FROM realm_signup WHERE username = #{username} AND status <> 'COMPLETED'")
    int countPending(String username);

    /** 全部已发放的秘境积分（后台概览用） */
    @Select("SELECT COALESCE(SUM(points), 0) FROM realm_signup WHERE status = 'COMPLETED'")
    int sumAllPoints();

    /** 全站待完成报名数（后台概览用） */
    @Select("SELECT COUNT(*) FROM realm_signup WHERE status <> 'COMPLETED'")
    int countAllPending();

    /** 全站已完成报名数（后台概览用） */
    @Select("SELECT COUNT(*) FROM realm_signup WHERE status = 'COMPLETED'")
    int countAllCompleted();
}
