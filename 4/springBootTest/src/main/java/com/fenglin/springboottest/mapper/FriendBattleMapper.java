package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.FriendBattle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FriendBattleMapper extends BaseMapper<FriendBattle> {

    /** 账号对战净得积分：胜局 stake 之和 - 败局 stake 之和（仅已结算） */
    @Select("SELECT COALESCE(SUM(CASE WHEN winner = #{username} THEN stake ELSE 0 END), 0) "
            + "- COALESCE(SUM(CASE WHEN winner IS NOT NULL AND winner <> #{username} THEN stake ELSE 0 END), 0) "
            + "FROM friend_battle "
            + "WHERE status = 'FINISHED' AND (challenger = #{username} OR opponent = #{username})")
    int netDelta(String username);
}
