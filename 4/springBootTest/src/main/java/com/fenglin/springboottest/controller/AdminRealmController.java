package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.IdsRequest;
import com.fenglin.springboottest.entity.RealmSignup;
import com.fenglin.springboottest.entity.SecretRealm;
import com.fenglin.springboottest.mapper.RealmSignupMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.RealmService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台管理 · 大荒秘境录（含报名记录）。
 */
@RestController
@RequestMapping("/api/admin/realms")
public class AdminRealmController {

    private static final List<String> GRADES = List.of("仙帝", "仙王", "真仙", "上古");
    private static final double RANK_MAX = 10.0;

    @Autowired
    private RealmService realmService;

    @Autowired
    private RealmSignupMapper signupMapper;

    @Autowired
    private AdminGuard adminGuard;

    @GetMapping("/list")
    public Result<List<SecretRealm>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        // 后台不区分当前用户，username 传 null 只填充报名人数
        return Result.ok(realmService.search(keyword, grade, sortBy, null));
    }

    @PostMapping
    public Result<SecretRealm> create(@RequestBody SecretRealm body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), null)) {
            return Result.fail(409, "秘境「" + body.getName() + "」已存在，请换一个");
        }
        body.setId(null);
        body.setCreatedAt(LocalDateTime.now());
        body.setSignedCount(null);
        body.setSigned(null);
        realmService.save(body);
        return Result.ok(realmService.detail(body.getId(), null));
    }

    @PutMapping("/{id}")
    public Result<SecretRealm> update(@PathVariable Long id, @RequestBody SecretRealm body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (realmService.getById(id) == null) {
            return Result.fail(404, "未找到该秘境，可能已被删除");
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), id)) {
            return Result.fail(409, "秘境「" + body.getName() + "」已被其他记录占用");
        }
        body.setId(id);
        body.setCreatedAt(null);
        body.setSignedCount(null);
        body.setSigned(null);
        realmService.updateById(body);
        return Result.ok(realmService.detail(id, null));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (realmService.getById(id) == null) {
            return Result.fail(404, "未找到该秘境，可能已被删除");
        }
        // 先清理报名记录，避免留下孤儿数据
        signupMapper.delete(Wrappers.<RealmSignup>lambdaQuery().eq(RealmSignup::getRealmId, id));
        realmService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    @Transactional
    public Result<Integer> batchDelete(@RequestBody IdsRequest body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            return Result.fail(400, "请先勾选要删除的秘境");
        }
        signupMapper.delete(Wrappers.<RealmSignup>lambdaQuery().in(RealmSignup::getRealmId, body.getIds()));
        boolean ok = realmService.removeByIds(body.getIds());
        return ok ? Result.ok(body.getIds().size()) : Result.fail(500, "批量删除失败，请重试");
    }

    @PostMapping("/{id}/toggle-hot")
    public Result<Integer> toggleHot(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        SecretRealm old = realmService.getById(id);
        if (old == null) {
            return Result.fail(404, "未找到该秘境，可能已被删除");
        }
        SecretRealm patch = new SecretRealm();
        patch.setId(id);
        patch.setHot(old.getHot() != null && old.getHot() == 1 ? 0 : 1);
        realmService.updateById(patch);
        return Result.ok(patch.getHot());
    }

    /** 某秘境的报名记录 */
    @GetMapping("/{id}/signups")
    public Result<List<RealmSignup>> signups(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(signupMapper.selectList(Wrappers.<RealmSignup>lambdaQuery()
                .eq(RealmSignup::getRealmId, id)
                .orderByDesc(RealmSignup::getId)));
    }

    /** 管理员强制取消某条报名记录 */
    @DeleteMapping("/signups/{signupId}")
    public Result<Void> deleteSignup(@PathVariable Long signupId, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (signupMapper.selectById(signupId) == null) {
            return Result.fail(404, "报名记录不存在或已被删除");
        }
        signupMapper.deleteById(signupId);
        return Result.ok();
    }

    // ---------- 内部工具 ----------

    private void normalize(SecretRealm r) {
        if (r.getName() != null) {
            r.setName(r.getName().trim());
        }
        if (r.getGrade() != null) {
            r.setGrade(r.getGrade().trim());
        }
        if (r.getGlyph() == null || r.getGlyph().isBlank()) {
            r.setGlyph("🌌");
        }
        if (r.getRankScore() == null) {
            r.setRankScore(0.0);
        }
        if (r.getQuota() == null || r.getQuota() < 0) {
            r.setQuota(0);
        }
        r.setHot(r.getHot() != null && r.getHot() == 1 ? 1 : 0);
    }

    private String validate(SecretRealm r) {
        String err = Validators.text(r.getName(), "秘境名称", 50);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(r.getGrade(), "秘境等级", GRADES);
        if (err != null) {
            return err;
        }
        err = Validators.range(r.getRankScore(), 0, RANK_MAX, "评级评分");
        if (err != null) {
            return err;
        }
        err = Validators.range(r.getQuota(), 0, 100000, "每期名额");
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(r.getGlyph(), "卡面图标", 8);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(r.getLocation(), "所在地", 60);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(r.getOpenTime(), "开启时间", 100);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(r.getDuration(), "开启时长", 20);
        if (err != null) {
            return err;
        }
        return Validators.optionalText(r.getDescription(), "秘境介绍", 600);
    }

    private boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        var q = Wrappers.<SecretRealm>lambdaQuery().eq(SecretRealm::getName, name);
        if (excludeId != null) {
            q.ne(SecretRealm::getId, excludeId);
        }
        return realmService.count(q) > 0;
    }
}
