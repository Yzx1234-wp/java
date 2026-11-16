package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 后台管理统一鉴权（按用户表 permission 字段）。
 * <p>两步校验：① 令牌有效（TokenStore 能查到 userId）；
 * ② 账号 permission == 0（管理员权限）。permission = 1（默认/普通修士）
 * 即使拿到自己的 token 也无法调用 /api/admin/* 接口。</p>
 */
@Component
public class AdminGuard {

    /** 管理员权限值 */
    public static final int PERMISSION_ADMIN = 0;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserService userService;

    /**
     * 校验后台权限。
     *
     * @return null 表示通过；否则返回带 code / message 的失败结果（401 未登录、403 无权限）
     */
    public Result<Void> check(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            // 区分「没登录」与「登录了但没权限」
            String token = parseToken(request.getHeader("Authorization"));
            Long uid = token == null ? null : tokenStore.getUserId(token);
            if (uid == null) {
                return Result.fail(401, "未登录或登录已失效，请重新登录");
            }
            return Result.fail(403, "账号没有后台管理权限（仅管理员权限可进入后台）");
        }
        return null;
    }

    /** 当前管理员账号；鉴权失败返回 null */
    public User currentAdmin(HttpServletRequest request) {
        return currentUser(request);
    }

    /** 是否管理员账号（按用户表 permission 字段判断） */
    public boolean isAdmin(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        return user != null && isPermissionAdmin(user);
    }

    /** permission == 0 即管理员 */
    public boolean isPermissionAdmin(User user) {
        return user != null && user.getPermission() != null && user.getPermission() == PERMISSION_ADMIN;
    }

    /* ===== 私有辅助 ===== */

    /** 由令牌解析当前用户；未登录 / 失效 / 无权限返回 null */
    private User currentUser(HttpServletRequest request) {
        String token = parseToken(request.getHeader("Authorization"));
        if (token == null || token.isBlank()) {
            return null;
        }
        Long uid = tokenStore.getUserId(token);
        if (uid == null) {
            return null;
        }
        User user = userService.getById(uid);
        if (user == null) {
            return null;
        }
        return isPermissionAdmin(user) ? user : null;
    }

    /** 解析 Authorization 头，兼容带 / 不带 Bearer 前缀 */
    public String parseToken(String auth) {
        if (auth == null) {
            return null;
        }
        String t = auth.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.startsWith("Bearer ")) {
            t = t.substring(7).trim();
        } else if (t.startsWith("Bearer")) {
            t = t.substring(6).trim();
        }
        return t.isEmpty() ? null : t;
    }
}
