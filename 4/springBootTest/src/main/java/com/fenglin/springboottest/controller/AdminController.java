package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Gongfa;
import com.fenglin.springboottest.service.GongfaService;
import com.fenglin.springboottest.service.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 后台管理接口：对首页「大荒功法库」展示的数据做增删改查。
 * <p>全部接口要求携带登录令牌（Authorization: Bearer &lt;token&gt;），令牌由 /api/auth/login 签发。</p>
 * <p>注意：Gongfa 实体的 JSON 字段名为 from / rank / desc / hot（与首页卡片一致），
 * 前端提交时需使用这些字段名。</p>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private GongfaService gongfaService;

    @Autowired
    private TokenStore tokenStore;

    /** 管理端列表：复用首页检索逻辑（关键词 + 分类 + 品阶 + 排序），返回全量便于前端做分页 */
    @GetMapping("/gongfa/list")
    public Result<List<Gongfa>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = checkAuth(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(gongfaService.search(keyword, cat, grade, sortBy));
    }

    /** 新增功法典籍 */
    @PostMapping("/gongfa")
    public Result<Gongfa> create(@RequestBody Gongfa body, HttpServletRequest request) {
        Result<Void> auth = checkAuth(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        String err = validate(body, false);
        if (err != null) {
            return Result.fail(err);
        }
        body.setId(null);
        body.setCreatedAt(LocalDateTime.now());
        gongfaService.save(body);
        return Result.ok(body);
    }

    /** 修改功法典籍 */
    @PutMapping("/gongfa/{id}")
    public Result<Gongfa> update(@PathVariable Long id, @RequestBody Gongfa body, HttpServletRequest request) {
        Result<Void> auth = checkAuth(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        Gongfa old = gongfaService.getById(id);
        if (old == null) {
            return Result.fail("未找到该功法典籍");
        }
        body.setId(id);
        String err = validate(body, true);
        if (err != null) {
            return Result.fail(err);
        }
        // 不允许通过更新改动创建时间
        body.setCreatedAt(null);
        gongfaService.updateById(body);
        return Result.ok(gongfaService.getById(id));
    }

    /** 删除功法典籍 */
    @DeleteMapping("/gongfa/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = checkAuth(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (gongfaService.getById(id) == null) {
            return Result.fail("未找到该功法典籍");
        }
        gongfaService.removeById(id);
        return Result.ok();
    }

    /** 校验登录令牌；通过返回 null，失败返回 401 的 Result */
    private Result<Void> checkAuth(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String token = parseToken(auth);
        if (token == null || tokenStore.getUserId(token) == null) {
            return Result.fail(401, "未登录或登录已失效");
        }
        return null;
    }

    /** 新增 / 修改的基础校验；返回 null 表示通过 */
    private String validate(Gongfa g, boolean isUpdate) {
        if (g.getName() == null || g.getName().trim().isEmpty()) {
            return "功法名称不能为空";
        }
        if (g.getName().trim().length() > 50) {
            return "功法名称过长";
        }
        if (!isUpdate && g.getRankScore() != null && (g.getRankScore() < 0 || g.getRankScore() > 100)) {
            return "品阶评分需在 0-100 之间";
        }
        return null;
    }

    private String parseToken(String auth) {
        if (auth == null) {
            return null;
        }
        String t = auth.trim();
        if (t.startsWith("Bearer ")) {
            return t.substring(7).trim();
        }
        if (t.startsWith("Bearer")) {
            return t.substring(6).trim();
        }
        return t;
    }
}
