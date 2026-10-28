package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.RealmSignup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RealmSignupMapper extends BaseMapper<RealmSignup> {

    /** 各秘境报名人数 */
    @Select("SELECT realm_id AS realmId, COUNT(*) AS cnt FROM realm_signup GROUP BY realm_id")
    List<Map<String, Object>> countByRealm();
}
