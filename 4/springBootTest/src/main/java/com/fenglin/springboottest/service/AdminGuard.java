package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台管理统一鉴权。
 * <p>两步校验：① 令牌有效（TokenStore 能查到 userId）；② 账号在管理员白名单内
 * （配置项 {@code admin.allow-users}，默认只有 admin）。
 * 这样即使普通用户拿到自己的 token，也无法调用 /api/admin/* 接口。</p>
 */
@Component
public class AdminGuard {

    private final TokenStore tokenStore;
    private final UserService userService;
    private final Set<String> allowUsers;

    public AdminGuard(TokenStore tokenStore,
                      UserService userService,
                      @Value("${admin.allow-users:admin}") String allowUsers) {
        this.tokenStore = tokenStore;
        this.userService = userService;
        this.allowUsers = Arrays.stream(allowUsers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 校验后台权限。
     *
     * @return null 表示通过；否则返回带 code / message 的失败结果（401 未登录、403 无权限）
     */
    public Result<Void> check(HttpServletRequest request) {
        String token = parseToken(request.getHeader("Authorization"));
        if (token == null) {
            return Result.fail(401, "未登录或登录已失效，请重新登录");
        }
        Long uid = tokenStore.getUserId(token);
        if (uid == null) {
            return Result.fail(401, "登录已失效，请重新登录");
        }
        User user = userService.getById(uid);
        if (user == null) {
            return Result.fail(401, "账号不存在，请重新登录");
        }
        if (!allowUsers.isEmpty() && !allowUsers.contains(user.getUsername())) {
            return Result.fail(403, "账号「" + user.getUsername() + "」没有后台管理权限");
        }
        return null;
    }

    /** 当前管理员账号；鉴权失败返回 null */
    public User currentAdmin(HttpServletRequest request) {
        String token = parseToken(request.getHeader("Authorization"));
        if (token == null) {
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
        if (!allowUsers.isEmpty() && !allowUsers.contains(user.getUsername())) {
            return null;
        }
        return user;
    }

    /** 是否管理员账号 */
    public boolean isAdmin(String username) {
        return username != null && (allowUsers.isEmpty() || allowUsers.contains(username));
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
