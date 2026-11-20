package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /** 各好友的未读消息数（接收人为我且未读） */
    @Select("SELECT sender AS friend, COUNT(*) AS cnt FROM chat_message "
            + "WHERE receiver = #{me} AND is_read = 0 GROUP BY sender")
    List<Map<String, Object>> unreadByFriend(String me);
}
