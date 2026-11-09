package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.AdminUserVO;
import com.fenglin.springboottest.controller.dto.PasswordRequest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.entity.RealmSignup;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理 · 修士账号（用户管理）。
 * <p>支持查看账号列表与统计、改昵称、重置密码、删除账号（连同其报名/接取记录）。
 * 管理员账号本身不可删除。</p>
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private UserService userService;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private AdminGuard adminGuard;

    /** 账号列表（附带积分 / 接取数 / 报名数） */
    @GetMapping("/list")
    public Result<List<AdminUserVO>> list(
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }

        var q = Wrappers.<User>lambdaQuery().orderByAsc(User::getId);
        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            q.and(w -> w.like(User::getUsername, k).or().like(User::getNickname, k));
        }
        List<User> users = userService.list(q);

        Map<String, Integer> realmCounts = new HashMap<>();
        Map<String, Integer> realmPoints = new HashMap<>();
        for (Map<String, Object> row : signupMapper.countByUser()) {
            String name = String.valueOf(row.get("username"));
            realmCounts.put(name, toInt(row.get("cnt")));
            realmPoints.put(name, toInt(row.get("pts")));
        }
        Map<String, Integer> questCounts = new HashMap<>();
        Map<String, Integer> pointsMap = new HashMap<>();
        for (Map<String, Object> row : acceptMapper.statsByUser()) {
            String name = String.valueOf(row.get("username"));
            questCounts.put(name, toInt(row.get("cnt")));
            pointsMap.put(name, toInt(row.get("pts")));
        }

        List<AdminUserVO> list = new ArrayList<>();
        for (User u : users) {
            AdminUserVO vo = new AdminUserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setCreatedAt(u.getCreatedAt());
            // 总积分 = 已确认完成的任务积分 + 已确认完成的秘境积分
            vo.setPoints(pointsMap.getOrDefault(u.getUsername(), 0)
                    + realmPoints.getOrDefault(u.getUsername(), 0));
            vo.setQuestCount(questCounts.getOrDefault(u.getUsername(), 0));
            vo.setRealmCount(realmCounts.getOrDefault(u.getUsername(), 0));
            vo.setAdmin(adminGuard.isAdmin(u.getUsername()));
            list.add(vo);
        }
        return Result.ok(list);
    }

    /** 修改昵称 */
    @PutMapping("/{id}")
    public Result<AdminUserVO> rename(@PathVariable Long id, @RequestBody User body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        User old = userService.getById(id);
        if (old == null) {
            return Result.fail(404, "账号不存在，可能已被删除");
        }
        String nickname = body == null || body.getNickname() == null ? "" : body.getNickname().trim();
        String err = Validators.text(nickname, "昵称", 64);
        if (err != null) {
            return Result.fail(400, err);
        }
        User patch = new User();
        patch.setId(id);
        patch.setNickname(nickname);
        userService.updateById(patch);

        AdminUserVO vo = new AdminUserVO();
        vo.setId(old.getId());
        vo.setUsername(old.getUsername());
        vo.setNickname(nickname);
        vo.setCreatedAt(old.getCreatedAt());
        vo.setAdmin(adminGuard.isAdmin(old.getUsername()));
        return Result.ok(vo);
    }

    /** 重置密码（管理员操作，无需旧密码） */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordRequest body,
                                      HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        User old = userService.getById(id);
        if (old == null) {
            return Result.fail(404, "账号不存在，可能已被删除");
        }
        String pw = body == null || body.getPassword() == null ? "" : body.getPassword().trim();
        if (pw.length() < 6 || pw.length() > 20) {
            return Result.fail(400, "新密码需 6-20 位");
        }
        User patch = new User();
        patch.setId(id);
        patch.setPassword(encoder.encode(pw));
        userService.updateById(patch);
        return Result.ok();
    }

    /** 删除账号（连同报名 / 接取记录一起清理） */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        User old = userService.getById(id);
        if (old == null) {
            return Result.fail(404, "账号不存在，可能已被删除");
        }
        if (adminGuard.isAdmin(old.getUsername())) {
            return Result.fail(403, "管理员账号不可删除");
        }
        String username = old.getUsername();
        signupMapper.delete(Wrappers.<RealmSignup>lambdaQuery().eq(RealmSignup::getUsername, username));
        acceptMapper.delete(Wrappers.<QuestAccept>lambdaQuery().eq(QuestAccept::getUsername, username));
        userService.removeById(id);
        return Result.ok();
    }

    private int toInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }
}
