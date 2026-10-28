package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.SecretRealm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SecretRealmMapper extends BaseMapper<SecretRealm> {

    /** 按秘境等级统计数量 */
    @Select("SELECT grade AS name, COUNT(*) AS cnt FROM secret_realm GROUP BY grade")
    List<Map<String, Object>> countByGrade();

    /** 热门秘境名称（按评级递降取前 6 个） */
    @Select("SELECT name FROM secret_realm WHERE is_hot = 1 ORDER BY rank_score DESC, id ASC LIMIT 6")
    List<String> selectHotNames();
}
