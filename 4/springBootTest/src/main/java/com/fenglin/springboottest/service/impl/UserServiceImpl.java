package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户注册 / 登录。
 * <p>密码按需求改为<b>明文存储、明文比对</b>（演示项目不做加密）；
 * 兼容旧库遗留的 BCrypt 哈希：登录成功后自动改写为明文（自愈迁移）。</p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 仅用于识别 / 校验旧库遗留的 BCrypt 哈希，新密码一律明文 */
    private final BCryptPasswordEncoder legacyEncoder = new BCryptPasswordEncoder();

    /** 默认权限：普通修士 */
    public static final int PERMISSION_DEFAULT = 1;

    /** 管理员权限 */
    public static final int PERMISSION_ADMIN = 0;

    @Override
    public int register(String username, String password) {
        Long count = lambdaQuery().eq(User::getUsername, username).count();
        if (count != null && count > 0) {
            return -1;
        }
        User user = new User();
        user.setUsername(username);
        // 明文存储（需求要求，演示项目不做加密）
        user.setPassword(password);
        user.setNickname(username);
        // 注册用户一律为默认权限（普通修士）
        user.setPermission(PERMISSION_DEFAULT);
        user.setCreatedAt(LocalDateTime.now());
        save(user);
        return 0;
    }

    @Override
    public User login(String username, String password) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            return null;
        }
        String stored = user.getPassword();
        if (stored == null) {
            return null;
        }
        if (stored.equals(password)) {
            return user;
        }
        // 旧库遗留的 BCrypt 哈希：仍允许用原密码登录，成功后改写为明文
        if (stored.startsWith("$2") && legacyEncoder.matches(password, stored)) {
            User patch = new User();
            patch.setId(user.getId());
            patch.setPassword(password);
            updateById(patch);
            user.setPassword(password);
            return user;
        }
        return null;
    }
}
