package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.service.FriendService;
import com.fenglin.springboottest.service.ShopService;
import com.fenglin.springboottest.service.TokenStore;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 「大荒交友录 · 好友」接口。
 * 全部需登录（Bearer token）。功能：添加好友 / 站内聊天 / 好友对战（自定义积分注，胜方得积分）。
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private ShopService shopService;

    /* ===== 好友 ===== */

    /** 检索修士（账号/昵称模糊），标注好友/申请状态 */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(friendService.searchUsers(keyword, me));
    }

    /** 发送好友申请 body: {"username": "..."} */
    @PostMapping("/request")
    public Result<Void> sendRequest(@RequestBody Map<String, String> body,
                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.sendRequest(body == null ? null : body.get("username"), me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 我的好友申请（incoming 收到的待处理 / outgoing 发出的待批） */
    @GetMapping("/requests")
    public Result<Map<String, Object>> requests(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(friendService.myRequests(me));
    }

    /** 通过好友申请 */
    @PostMapping("/request/{id}/accept")
    public Result<Void> acceptRequest(@PathVariable Long id,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.acceptRequest(id, me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 拒绝好友申请 */
    @PostMapping("/request/{id}/decline")
    public Result<Void> declineRequest(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.declineRequest(id, me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 好友列表 */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(friendService.myFriends(me));
    }

    /** 删除好友 */
    @DeleteMapping("/{username}")
    public Result<Void> remove(@PathVariable String username,
                               @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.removeFriend(username, me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /* ===== 聊天 ===== */

    /** 与某好友的聊天记录（afterId 增量），同时标记已读 */
    @GetMapping("/chat/{username}")
    public Result<List<Map<String, Object>>> chat(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "0") Long afterId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            return Result.ok(friendService.chatHistory(username, afterId, me));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 发送私信 body: {"content": "..."} */
    @PostMapping("/chat/{username}")
    public Result<Map<String, Object>> sendChat(@PathVariable String username,
                                                @RequestBody Map<String, String> body,
                                                @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            Long id = friendService.sendMessage(username, body == null ? null : body.get("content"), me);
            return Result.ok(Map.of("id", id));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 各好友未读消息数（好友列表角标用） */
    @GetMapping("/chat/unread")
    public Result<Map<String, Long>> unread(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(friendService.unreadCounts(me));
    }

    /* ===== 好友对战 ===== */

    /** 我的对战列表 */
    @GetMapping("/battle/list")
    public Result<List<Map<String, Object>>> battleList(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(friendService.myBattles(me));
    }

    /** 发起对战 body: {"friend": "...", "game": "CHESS|GOMOKU", "stake": 100} */
    @PostMapping("/battle")
    public Result<Map<String, Object>> createBattle(@RequestBody Map<String, Object> body,
                                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        if (body == null || body.get("friend") == null || body.get("game") == null) {
            return Result.fail("缺少对战参数（好友 / 项目）");
        }
        int stake;
        try {
            stake = Integer.parseInt(String.valueOf(body.get("stake")));
        } catch (Exception e) {
            return Result.fail("积分注必须为正整数");
        }
        try {
            return Result.ok(friendService.createBattle(
                    String.valueOf(body.get("friend")),
                    String.valueOf(body.get("game")),
                    stake, me, currentPoints(me)));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 应战 */
    @PostMapping("/battle/{id}/accept")
    public Result<Void> acceptBattle(@PathVariable Long id,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.acceptBattle(id, me, currentPoints(me));
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 拒应对战 / 发起人取消 */
    @PostMapping("/battle/{id}/decline")
    public Result<Void> declineBattle(@PathVariable Long id,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.declineBattle(id, me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 对战详情（含招法、轮次、我方角色） */
    @GetMapping("/battle/{id}")
    public Result<Map<String, Object>> battleDetail(@PathVariable Long id,
                                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            return Result.ok(friendService.battleDetail(id, me));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 提交招法 body: {"move": "r,c"} */
    @PostMapping("/battle/{id}/move")
    public Result<Void> battleMove(@PathVariable Long id,
                                   @RequestBody Map<String, String> body,
                                   @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        try {
            friendService.battleMove(id, body == null ? null : body.get("move"), me);
            return Result.ok();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 上报结果 body: {"result": "WIN|LOSE"}；结算后按注金计入积分 */
    @PostMapping("/battle/{id}/finish")
    public Result<Map<String, Object>> battleFinish(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        String me = currentUsername(auth);
        if (me == null) {
            return Result.fail(401, "请先登录");
        }
        String result = body == null ? null : body.get("result");
        if (!"WIN".equals(result) && !"LOSE".equals(result)) {
            return Result.fail("结果只能为 WIN 或 LOSE");
        }
        try {
            return Result.ok(friendService.battleFinish(id, "WIN".equals(result), me));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /* ===== 私有辅助 ===== */

    /** 由 Bearer token 解析当前账号 */
    private String currentUsername(String auth) {
        String token = parseToken(auth);
        if (token == null || token.isBlank()) {
            return null;
        }
        Long uid = tokenStore.getUserId(token);
        if (uid == null) {
            return null;
        }
        User user = userService.getById(uid);
        return user == null ? null : user.getUsername();
    }

    /** 可用积分 = 任务/秘境累计发放 + 对战胜负净得 - 商店兑换消耗（与任务页 /points 同口径） */
    private int currentPoints(String me) {
        int earned = acceptMapper.sumPoints(me) + signupMapper.sumPoints(me);
        int spent = shopService.spentPoints(me);
        int battleDelta = friendService.battleDelta(me);
        return Math.max(0, earned + battleDelta - spent);
    }

    private String parseToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        return auth.substring(7).trim();
    }
}
