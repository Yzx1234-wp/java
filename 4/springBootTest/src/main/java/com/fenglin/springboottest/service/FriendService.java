package com.fenglin.springboottest.service;

import com.fenglin.springboottest.entity.FriendBattle;

import java.util.List;
import java.util.Map;
/**
 * 「大荒交友录 · 好友」接口。
 * 好友添加 / 站内聊天 / 好友对战（象棋、五子棋，自定义积分注，胜方得积分）。
 * 积分口径：可用积分 = 任务/秘境累计发放 + 对战胜负净得 - 商店兑换消耗。
 */
public interface FriendService {

    /* ===== 好友 ===== */

    /** 按关键词检索修士（账号/昵称），标注 是否好友/申请状态；排除自己 */
    List<Map<String, Object>> searchUsers(String keyword, String me);

    /** 发送好友申请（不可重复、不可加自己、已是好友报错） */
    void sendRequest(String to, String me);

    /** 我的好友申请：incoming 待我处理 / outgoing 我发出的待批 */
    Map<String, Object> myRequests(String me);

    /** 通过好友申请：双方各写一行 friend */
    void acceptRequest(Long requestId, String me);

    /** 拒绝好友申请 */
    void declineRequest(Long requestId, String me);

    /** 好友列表（带昵称与未读消息数） */
    List<Map<String, Object>> myFriends(String me);

    /** 删除好友（双向移除） */
    void removeFriend(String friend, String me);

    /* ===== 聊天 ===== */

    /** 发送私信（仅限好友；返回消息 id） */
    Long sendMessage(String to, String content, String me);

    /** 与某好友的聊天记录（afterId 增量拉取）；同时把对方发给我的消息标记已读 */
    List<Map<String, Object>> chatHistory(String friend, Long afterId, String me);

    /** 各好友未读消息数 */
    Map<String, Long> unreadCounts(String me);

    /* ===== 好友对战 ===== */

    /** 发起对战：校验好友关系 + 双方积分足够；status=REQUESTED */
    Map<String, Object> createBattle(String friend, String game, int stake, String me, int myPoints);

    /** 我的相关对战列表（发起的 + 应战的，新在前） */
    List<Map<String, Object>> myBattles(String me);

    /** 应战：校验积分足够；status=READY，双方进入对战 */
    void acceptBattle(Long battleId, String me, int myPoints);

    /** 拒应对战 / 发起人取消：status=CANCELLED */
    void declineBattle(Long battleId, String me);

    /** 对战详情：moves 解析为 List，附我方角色与轮到谁 */
    Map<String, Object> battleDetail(Long battleId, String me);

    /** 提交招法：校验参战人 / 对战中 / 轮次（奇偶），追加到 moves */
    void battleMove(Long battleId, String move, String me);

    /** 上报结果（WIN 我胜 / LOSE 我负）：结算 status=FINISHED 并回填胜方 */
    Map<String, Object> battleFinish(Long battleId, boolean iWin, String me);

    /** 账号对战净得积分：胜局 stake 之和 - 败局 stake 之和（仅已结算） */
    int battleDelta(String username);
}
