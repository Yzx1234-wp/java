package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.IdsRequest;
import com.fenglin.springboottest.entity.Gongfa;
import com.fenglin.springboottest.entity.GongfaCategory;
import com.fenglin.springboottest.mapper.GongfaCategoryMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.GongfaService;
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
 * 后台管理 · 大荒功法库。
 * <p>全部接口需管理员令牌（Authorization: Bearer &lt;token&gt;）。
 * 注意 Gongfa 的 JSON 字段名为 from / rank / desc / hot（与首页卡片一致）。</p>
 */
@RestController
@RequestMapping("/api/admin/gongfa")
public class AdminController {

    /** 品阶分级候选值 */
    private static final List<String> GRADES = List.of("绝世", "绝顶", "一流", "上乘", "中乘");

    /** 分类字典为空时的兜底候选值 */
    private static final List<String> FALLBACK_CATS = List.of("功法", "宝术", "至尊术", "剑诀", "秘法", "身法");

    /** 评分量纲：与首页数据一致，0-10（保留一位小数） */
    private static final double RANK_MAX = 10.0;

    @Autowired
    private GongfaService gongfaService;

    @Autowired
    private GongfaCategoryMapper categoryMapper;

    @Autowired
    private AdminGuard adminGuard;

    /** 管理端列表：支持关键词 / 分类 / 品阶 / 排序 */
    @GetMapping("/list")
    public Result<List<Gongfa>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(gongfaService.search(keyword, cat, grade, sortBy));
    }

    /** 分类字典（供新增 / 编辑表单下拉使用） */
    @GetMapping("/categories")
    public Result<List<GongfaCategory>> categories(HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        List<GongfaCategory> list = categoryMapper.selectList(
                Wrappers.<GongfaCategory>lambdaQuery().orderByAsc(GongfaCategory::getSortNo));
        if (list == null || list.isEmpty()) {
            list = FALLBACK_CATS.stream().map(code -> {
                GongfaCategory c = new GongfaCategory();
                c.setCode(code);
                c.setName(code);
                c.setSortNo(0);
                return c;
            }).toList();
        }
        return Result.ok(list);
    }

    /** 新增功法典籍 */
    @PostMapping
    public Result<Gongfa> create(@RequestBody Gongfa body, HttpServletRequest request) {
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
            return Result.fail(409, "功法名称「" + body.getName() + "」已存在，请换一个");
        }
        body.setId(null);
        body.setCreatedAt(LocalDateTime.now());
        gongfaService.save(body);
        return Result.ok(gongfaService.getById(body.getId()));
    }

    /** 修改功法典籍 */
    @PutMapping("/{id}")
    public Result<Gongfa> update(@PathVariable Long id, @RequestBody Gongfa body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        Gongfa old = gongfaService.getById(id);
        if (old == null) {
            return Result.fail(404, "未找到该功法典籍，可能已被删除");
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), id)) {
            return Result.fail(409, "功法名称「" + body.getName() + "」已被其他典籍占用");
        }
        body.setId(id);
        // 创建时间不允许通过编辑修改
        body.setCreatedAt(null);
        gongfaService.updateById(body);
        return Result.ok(gongfaService.getById(id));
    }

    /** 删除功法典籍 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (gongfaService.getById(id) == null) {
            return Result.fail(404, "未找到该功法典籍，可能已被删除");
        }
        gongfaService.removeById(id);
        return Result.ok();
    }

    /** 批量删除 */
    @PostMapping("/batch-delete")
    public Result<Integer> batchDelete(@RequestBody IdsRequest body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            return Result.fail(400, "请先勾选要删除的典籍");
        }
        boolean ok = gongfaService.removeByIds(body.getIds());
        return ok ? Result.ok(body.getIds().size()) : Result.fail(500, "批量删除失败，请重试");
    }

    /** 切换热门标记 */
    @PostMapping("/{id}/toggle-hot")
    public Result<Integer> toggleHot(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        Gongfa old = gongfaService.getById(id);
        if (old == null) {
            return Result.fail(404, "未找到该功法典籍，可能已被删除");
        }
        Gongfa patch = new Gongfa();
        patch.setId(id);
        patch.setHot(old.getHot() != null && old.getHot() == 1 ? 0 : 1);
        gongfaService.updateById(patch);
        return Result.ok(patch.getHot());
    }

    // ---------- 内部工具 ----------

    /** 统一去空格 / 补默认值 / 规范 hot 取值 */
    private void normalize(Gongfa g) {
        if (g.getName() != null) {
            g.setName(g.getName().trim());
        }
        if (g.getCat() != null) {
            g.setCat(g.getCat().trim());
        }
        if (g.getGrade() != null) {
            g.setGrade(g.getGrade().trim());
        }
        if (g.getGlyph() == null || g.getGlyph().isBlank()) {
            g.setGlyph("✦");
        }
        if (g.getRankScore() == null) {
            g.setRankScore(0.0);
        }
        g.setHot(g.getHot() != null && g.getHot() == 1 ? 1 : 0);
    }

    /** 表单校验；返回 null 表示通过 */
    private String validate(Gongfa g) {
        String err = Validators.text(g.getName(), "功法名称", 50);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(g.getCat(), "分类", cats());
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(g.getGrade(), "品阶分级", GRADES);
        if (err != null) {
            return err;
        }
        err = Validators.range(g.getRankScore(), 0, RANK_MAX, "品阶评分");
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(g.getGlyph(), "卡面图标", 8);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(g.getTier(), "品阶 / 流派简述", 60);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(g.getOrigin(), "传承来源", 40);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(g.getOwner(), "修习者", 40);
        if (err != null) {
            return err;
        }
        return Validators.optionalText(g.getDescription(), "功法简介", 300);
    }

    /** 分类候选值（优先取字典表） */
    private List<String> cats() {
        List<GongfaCategory> dict = categoryMapper.selectList(null);
        if (dict == null || dict.isEmpty()) {
            return FALLBACK_CATS;
        }
        List<String> codes = dict.stream().map(GongfaCategory::getCode).filter(c -> c != null && !c.isBlank()).toList();
        return codes.isEmpty() ? FALLBACK_CATS : codes;
    }

    /** 名称是否已被占用（excludeId 用于编辑时排除自身） */
    private boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        var q = Wrappers.<Gongfa>lambdaQuery().eq(Gongfa::getName, name);
        if (excludeId != null) {
            q.ne(Gongfa::getId, excludeId);
        }
        return gongfaService.count(q) > 0;
    }
}
