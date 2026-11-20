package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.entity.ChatMessage;
import com.fenglin.springboottest.entity.Friend;
import com.fenglin.springboottest.entity.FriendBattle;
import com.fenglin.springboottest.entity.FriendRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.ChatMessageMapper;
import com.fenglin.springboottest.mapper.FriendBattleMapper;
import com.fenglin.springboottest.mapper.FriendMapper;
import com.fenglin.springboottest.mapper.FriendRequestMapper;
import com.fenglin.springboottest.mapper.ShopOrderMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 「大荒交友录」实现。
 * 仅依赖 Mapper 层计算积分，避免与 Quest/Shop 服务形成循环依赖。
 */
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendRequestMapper requestMapper;

    @Autowired
    private ChatMessageMapper chatMapper;

    @Autowired
    private FriendBattleMapper battleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Autowired
    private com.fenglin.springboottest.mapper.QuestAcceptMapper acceptMapper;

    @Autowired
    private com.fenglin.springboottest.mapper.RealmSignupMapper signupMapper;

    private final ObjectMapper json = new ObjectMapper();

    /* ===== 好友 ===== */

    @Override
    public List<Map<String, Object>> searchUsers(String keyword, String me) {
        String k = keyword == null ? "" : keyword.trim();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> q =
                Wrappers.<User>lambdaQuery().ne(User::getUsername, me);
        if (!k.isEmpty()) {
            q.and(w -> w.like(User::getUsername, k).or().like(User::getNickname, k));
        }
        q.last("LIMIT 30");
        List<User> users = userMapper.selectList(q);
        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname());
            m.put("isFriend", isFriend(u.getUsername(), me));
            FriendRequest req = findRequest(u.getUsername(), me);
            m.put("requestStatus", req == null ? null : req.getStatus());
            m.put("requestId", req == null ? null : req.getId());
            m.put("requestDir", req == null ? null : (req.getFromUser().equals(me) ? "OUT" : "IN"));
            out.add(m);
        }
        return out;
    }

    @Override
    public void sendRequest(String to, String me) {
        if (to == null || to.isBlank() || to.equals(me)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        User target = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, to));
        if (target == null) {
            throw new IllegalArgumentException("修士「" + to + "」不存在");
        }
        if (isFriend(to, me)) {
            throw new IllegalArgumentException("你们已经是好友了");
        }
        FriendRequest exist = findRequest(to, me);
        if (exist == null) {
            exist = new FriendRequest();
            exist.setFromUser(me);
            exist.setToUser(to);
            exist.setStatus(FriendRequest.PENDING);
            requestMapper.insert(exist);
        } else if (exist.getStatus().equals(FriendRequest.DECLINED)
                || exist.getStatus().equals(FriendRequest.ACCEPTED)) {
            // 拒绝后可重新申请；已通过不该走到这里（前面已拦截），兜底重置
            exist.setFromUser(me);
            exist.setToUser(to);
            exist.setStatus(FriendRequest.PENDING);
            exist.setCreatedAt(LocalDateTime.now());
            requestMapper.updateById(exist);
        } else if (exist.getFromUser().equals(me)) {
            throw new IllegalArgumentException("已发送过申请，等待对方通过");
        } else {
            throw new IllegalArgumentException("对方已向你发起好友申请，请在「收到的申请」里处理");
        }
    }

    @Override
    public Map<String, Object> myRequests(String me) {
        List<Map<String, Object>> incoming = new ArrayList<>();
        for (FriendRequest r : requestMapper.selectList(Wrappers.<FriendRequest>lambdaQuery()
                .eq(FriendRequest::getToUser, me)
                .eq(FriendRequest::getStatus, FriendRequest.PENDING)
                .orderByDesc(FriendRequest::getId))) {
            incoming.add(requestVO(r, me));
        }
        List<Map<String, Object>> outgoing = new ArrayList<>();
        for (FriendRequest r : requestMapper.selectList(Wrappers.<FriendRequest>lambdaQuery()
                .eq(FriendRequest::getFromUser, me)
                .eq(FriendRequest::getStatus, FriendRequest.PENDING)
                .orderByDesc(FriendRequest::getId))) {
            outgoing.add(requestVO(r, me));
        }
        return Map.of("incoming", incoming, "outgoing", outgoing);
    }

    @Override
    public void acceptRequest(Long requestId, String me) {
        FriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getToUser().equals(me)) {
            throw new IllegalArgumentException("申请不存在或不属于你");
        }
        if (!req.getStatus().equals(FriendRequest.PENDING)) {
            throw new IllegalArgumentException("该申请已处理过");
        }
        req.setStatus(FriendRequest.ACCEPTED);
        requestMapper.updateById(req);
        addFriendRow(req.getFromUser(), req.getToUser());
        addFriendRow(req.getToUser(), req.getFromUser());
    }

    @Override
    public void declineRequest(Long requestId, String me) {
        FriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getToUser().equals(me)) {
            throw new IllegalArgumentException("申请不存在或不属于你");
        }
        req.setStatus(FriendRequest.DECLINED);
        requestMapper.updateById(req);
    }

    @Override
    public List<Map<String, Object>> myFriends(String me) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Friend f : friendMapper.selectList(Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getOwner, me)
                .orderByAsc(Friend::getId))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("username", f.getFriend());
            User u = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, f.getFriend()));
            m.put("nickname", u == null || u.getNickname() == null || u.getNickname().isBlank()
                    ? f.getFriend() : u.getNickname());
            out.add(m);
        }
        return out;
    }

    @Override
    public void removeFriend(String friend, String me) {
        friendMapper.delete(Wrappers.<Friend>lambdaQuery().eq(Friend::getOwner, me).eq(Friend::getFriend, friend));
        friendMapper.delete(Wrappers.<Friend>lambdaQuery().eq(Friend::getOwner, friend).eq(Friend::getFriend, me));
    }

    /* ===== 聊天 ===== */

    @Override
    public Long sendMessage(String to, String content, String me) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("消息最长 500 字");
        }
        if (!isFriend(to, me)) {
            throw new IllegalArgumentException("只能给好友发消息");
        }
        ChatMessage msg = new ChatMessage();
        msg.setSender(me);
        msg.setReceiver(to);
        msg.setContent(content.trim());
        msg.setIsRead(0);
        chatMapper.insert(msg);
        return msg.getId();
    }

    @Override
    public List<Map<String, Object>> chatHistory(String friend, Long afterId, String me) {
        if (!isFriend(friend, me)) {
            throw new IllegalArgumentException("只能查看与好友的聊天");
        }
        List<ChatMessage> list = chatMapper.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .and(k -> k.and(x -> x.eq(ChatMessage::getSender, me).eq(ChatMessage::getReceiver, friend))
                        .or().and(x -> x.eq(ChatMessage::getSender, friend).eq(ChatMessage::getReceiver, me)))
                .gt(afterId != null && afterId > 0, ChatMessage::getId, afterId)
                .orderByAsc(ChatMessage::getId)
                .last("LIMIT 200"));
        // 对方发来的消息标记已读
        chatMapper.update(Wrappers.<ChatMessage>lambdaUpdate()
                .eq(ChatMessage::getSender, friend)
                .eq(ChatMessage::getReceiver, me)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChatMessage msg : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", msg.getId());
            m.put("sender", msg.getSender());
            m.put("receiver", msg.getReceiver());
            m.put("content", msg.getContent());
            m.put("mine", msg.getSender().equals(me));
            m.put("createdAt", msg.getCreatedAt() == null ? null : msg.getCreatedAt().toString());
            out.add(m);
        }
        return out;
    }

    @Override
    public Map<String, Long> unreadCounts(String me) {
        Map<String, Long> out = new HashMap<>();
        for (Map<String, Object> row : chatMapper.unreadByFriend(me)) {
            Object friend = row.get("friend");
            Object cnt = row.get("cnt");
            if (friend != null) {
                out.put(String.valueOf(friend), cnt instanceof Number ? ((Number) cnt).longValue() : 0L);
            }
        }
        return out;
    }

    /* ===== 好友对战 ===== */

    @Override
    public Map<String, Object> createBattle(String friend, String game, int stake, String me, int myPoints) {
        if (!isFriend(friend, me)) {
            throw new IllegalArgumentException("只能向好友发起对战");
        }
        if (!game.equals(FriendBattle.GAME_CHESS) && !game.equals(FriendBattle.GAME_GOMOKU)) {
            throw new IllegalArgumentException("对局项目仅支持 CHESS（象棋）或 GOMOKU（五子棋）");
        }
        if (stake <= 0) {
            throw new IllegalArgumentException("积分注必须大于 0");
        }
        if (stake > myPoints) {
            throw new IllegalArgumentException("你的可用积分不足（当前 " + myPoints + "）");
        }
        FriendBattle b = new FriendBattle();
        b.setChallenger(me);
        b.setOpponent(friend);
        b.setGame(game);
        b.setStake(stake);
        b.setStatus(FriendBattle.STATUS_REQUESTED);
        b.setMoves("[]");
        battleMapper.insert(b);
        return battleVO(b, me);
    }

    @Override
    public List<Map<String, Object>> myBattles(String me) {
        List<FriendBattle> list = new ArrayList<>();
        list.addAll(battleMapper.selectList(Wrappers.<FriendBattle>lambdaQuery()
                .eq(FriendBattle::getChallenger, me)
                .orderByDesc(FriendBattle::getId)
                .last("LIMIT 50")));
        list.addAll(battleMapper.selectList(Wrappers.<FriendBattle>lambdaQuery()
                .eq(FriendBattle::getOpponent, me)
                .orderByDesc(FriendBattle::getId)
                .last("LIMIT 50")));
        list.sort(Comparator.comparing(FriendBattle::getId).reversed());
        List<Map<String, Object>> out = new ArrayList<>();
        for (FriendBattle b : list) {
            out.add(battleVO(b, me));
        }
        return out;
    }

    @Override
    public void acceptBattle(Long battleId, String me, int myPoints) {
        FriendBattle b = mustBeOpponent(battleId, me);
        if (!b.getStatus().equals(FriendBattle.STATUS_REQUESTED)) {
            throw new IllegalArgumentException("该对战已处理过（当前状态 " + b.getStatus() + "）");
        }
        if (b.getStake() > myPoints) {
            throw new IllegalArgumentException("你的可用积分不足（注金 " + b.getStake() + "，当前 " + myPoints + "）");
        }
        b.setStatus(FriendBattle.STATUS_READY);
        battleMapper.updateById(b);
    }

    @Override
    public void declineBattle(Long battleId, String me) {
        FriendBattle b = battleMapper.selectById(battleId);
        if (b == null || (!b.getChallenger().equals(me) && !b.getOpponent().equals(me))) {
            throw new IllegalArgumentException("对战不存在或不属于你");
        }
        if (b.getStatus().equals(FriendBattle.STATUS_READY)) {
            throw new IllegalArgumentException("对战已开始，不能取消，请走完或认输");
        }
        if (b.getStatus().equals(FriendBattle.STATUS_FINISHED)) {
            throw new IllegalArgumentException("对战已结算");
        }
        b.setStatus(FriendBattle.STATUS_CANCELLED);
        battleMapper.updateById(b);
    }

    @Override
    public Map<String, Object> battleDetail(Long battleId, String me) {
        FriendBattle b = battleMapper.selectById(battleId);
        if (b == null || (!b.getChallenger().equals(me) && !b.getOpponent().equals(me))) {
            throw new IllegalArgumentException("对战不存在或不属于你");
        }
        Map<String, Object> vo = battleVO(b, me);
        vo.put("moves", parseMoves(b.getMoves()));
        vo.put("myTurn", myTurn(b, me));
        vo.put("myRole", b.getChallenger().equals(me) ? "challenger" : "opponent");
        return vo;
    }

    @Override
    public void battleMove(Long battleId, String move, String me) {
        if (move == null || move.isBlank() || move.length() > 32) {
            throw new IllegalArgumentException("招法格式错误");
        }
        FriendBattle b = battleMapper.selectById(battleId);
        if (b == null || (!b.getChallenger().equals(me) && !b.getOpponent().equals(me))) {
            throw new IllegalArgumentException("对战不存在或不属于你");
        }
        if (!b.getStatus().equals(FriendBattle.STATUS_READY)) {
            throw new IllegalArgumentException("对战不在进行中");
        }
        if (!myTurn(b, me)) {
            throw new IllegalArgumentException("还没轮到你走棋");
        }
        List<String> moves = parseMoves(b.getMoves());
        moves.add(move.trim());
        try {
            b.setMoves(json.writeValueAsString(moves));
        } catch (Exception e) {
            throw new IllegalArgumentException("招法序列化失败");
        }
        battleMapper.updateById(b);
    }

    @Override
    public Map<String, Object> battleFinish(Long battleId, boolean iWin, String me) {
        FriendBattle b = battleMapper.selectById(battleId);
        if (b == null || (!b.getChallenger().equals(me) && !b.getOpponent().equals(me))) {
            throw new IllegalArgumentException("对战不存在或不属于你");
        }
        if (!b.getStatus().equals(FriendBattle.STATUS_READY)) {
            throw new IllegalArgumentException("对战不在进行中，无法上报结果");
        }
        String other = b.getChallenger().equals(me) ? b.getOpponent() : b.getChallenger();
        b.setWinner(iWin ? me : other);
        b.setStatus(FriendBattle.STATUS_FINISHED);
        b.setFinishedAt(LocalDateTime.now());
        battleMapper.updateById(b);
        Map<String, Object> vo = battleVO(b, me);
        vo.put("myDelta", me.equals(b.getWinner()) ? b.getStake() : -b.getStake());
        return vo;
    }

    @Override
    public int battleDelta(String username) {
        return username == null || username.isBlank() ? 0 : battleMapper.netDelta(username);
    }

    /* ===== 私有辅助 ===== */

    private boolean isFriend(String other, String me) {
        return friendMapper.selectCount(Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getOwner, me)
                .eq(Friend::getFriend, other)) > 0;
    }

    private void addFriendRow(String owner, String friend) {
        if (friendMapper.selectCount(Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getOwner, owner).eq(Friend::getFriend, friend)) == 0) {
            Friend f = new Friend();
            f.setOwner(owner);
            f.setFriend(friend);
            friendMapper.insert(f);
        }
    }

    private FriendRequest findRequest(String other, String me) {
        List<FriendRequest> list = requestMapper.selectList(Wrappers.<FriendRequest>lambdaQuery()
                .and(k -> k.and(x -> x.eq(FriendRequest::getFromUser, me).eq(FriendRequest::getToUser, other))
                        .or().and(x -> x.eq(FriendRequest::getFromUser, other).eq(FriendRequest::getToUser, me)))
                .orderByDesc(FriendRequest::getId)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    private Map<String, Object> requestVO(FriendRequest r, String me) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("status", r.getStatus());
        m.put("dir", r.getFromUser().equals(me) ? "OUT" : "IN");
        m.put("createdAt", r.getCreatedAt() == null ? null : r.getCreatedAt().toString());
        if (r.getFromUser().equals(me)) {
            m.put("user", r.getToUser());
            m.put("userNickname", nicknameOf(r.getToUser()));
        } else {
            m.put("user", r.getFromUser());
            m.put("userNickname", nicknameOf(r.getFromUser()));
        }
        return m;
    }

    private Map<String, Object> battleVO(FriendBattle b, String me) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("challenger", b.getChallenger());
        m.put("opponent", b.getOpponent());
        m.put("game", b.getGame());
        m.put("stake", b.getStake());
        m.put("status", b.getStatus());
        m.put("winner", b.getWinner());
        m.put("createdAt", b.getCreatedAt() == null ? null : b.getCreatedAt().toString());
        m.put("finishedAt", b.getFinishedAt() == null ? null : b.getFinishedAt().toString());
        boolean mine = b.getChallenger().equals(me);
        m.put("myRole", mine ? "challenger" : "opponent");
        m.put("mySide", b.getChallenger().equals(me));
        m.put("counterpart", mine ? b.getOpponent() : b.getChallenger());
        m.put("counterpartNickname", nicknameOf(mine ? b.getOpponent() : b.getChallenger()));
        m.put("iWon", Objects.equals(b.getWinner(), me));
        return m;
    }

    private String nicknameOf(String username) {
        User u = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        return u == null || u.getNickname() == null || u.getNickname().isBlank() ? username : u.getNickname();
    }

    /** 轮次：moves 偶数条 → 挑战者（先手）行棋；奇数条 → 应战者 */
    private boolean myTurn(FriendBattle b, String me) {
        int count = parseMoves(b.getMoves()).size();
        boolean challengerTurn = count % 2 == 0;
        return challengerTurn ? b.getChallenger().equals(me) : b.getOpponent().equals(me);
    }

    private FriendBattle mustBeOpponent(Long battleId, String me) {
        FriendBattle b = battleMapper.selectById(battleId);
        if (b == null || !b.getOpponent().equals(me)) {
            throw new IllegalArgumentException("对战不存在或你不是应战方");
        }
        return b;
    }

    private List<String> parseMoves(String jsonMoves) {
        if (jsonMoves == null || jsonMoves.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<?> raw = json.readValue(jsonMoves, List.class);
            List<String> out = new ArrayList<>();
            for (Object o : raw) {
                out.add(String.valueOf(o));
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
