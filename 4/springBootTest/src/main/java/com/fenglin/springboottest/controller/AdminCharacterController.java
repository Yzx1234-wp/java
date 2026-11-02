package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.IdsRequest;
import com.fenglin.springboottest.controller.dto.OptionVO;
import com.fenglin.springboottest.entity.WorldCharacter;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.CharacterService;
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
 * 后台管理 · 大荒人物志。
 * <p>JSON 字段名与前台一致：rankScore→rank、description 原样、isHot→hot。</p>
 */
@RestController
@RequestMapping("/api/admin/characters")
public class AdminCharacterController {

    private static final List<String> GRADES = List.of("仙帝", "准仙帝", "仙王", "真仙", "尊者");
    private static final double RANK_MAX = 10.0;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private AdminGuard adminGuard;

    @GetMapping("/list")
    public Result<List<WorldCharacter>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String faction,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(characterService.search(keyword, faction, grade, sortBy));
    }

    /** 势力筛选项（含数量），供后台筛选下拉 */
    @GetMapping("/factions")
    public Result<List<OptionVO>> factions(HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(characterService.factions());
    }

    @PostMapping
    public Result<WorldCharacter> create(@RequestBody WorldCharacter body, HttpServletRequest request) {
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
            return Result.fail(409, "人物「" + body.getName() + "」已存在，请换一个");
        }
        body.setId(null);
        body.setCreatedAt(LocalDateTime.now());
        characterService.save(body);
        return Result.ok(characterService.getById(body.getId()));
    }

    @PutMapping("/{id}")
    public Result<WorldCharacter> update(@PathVariable Long id, @RequestBody WorldCharacter body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (characterService.getById(id) == null) {
            return Result.fail(404, "未找到该人物，可能已被删除");
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), id)) {
            return Result.fail(409, "人物「" + body.getName() + "」已被其他记录占用");
        }
        body.setId(id);
        body.setCreatedAt(null);
        characterService.updateById(body);
        return Result.ok(characterService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (characterService.getById(id) == null) {
            return Result.fail(404, "未找到该人物，可能已被删除");
        }
        characterService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    public Result<Integer> batchDelete(@RequestBody IdsRequest body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            return Result.fail(400, "请先勾选要删除的人物");
        }
        boolean ok = characterService.removeByIds(body.getIds());
        return ok ? Result.ok(body.getIds().size()) : Result.fail(500, "批量删除失败，请重试");
    }

    @PostMapping("/{id}/toggle-hot")
    public Result<Integer> toggleHot(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        WorldCharacter old = characterService.getById(id);
        if (old == null) {
            return Result.fail(404, "未找到该人物，可能已被删除");
        }
        WorldCharacter patch = new WorldCharacter();
        patch.setId(id);
        patch.setHot(old.getHot() != null && old.getHot() == 1 ? 0 : 1);
        characterService.updateById(patch);
        return Result.ok(patch.getHot());
    }

    // ---------- 内部工具 ----------

    private void normalize(WorldCharacter c) {
        if (c.getName() != null) {
            c.setName(c.getName().trim());
        }
        if (c.getGrade() != null) {
            c.setGrade(c.getGrade().trim());
        }
        if (c.getGlyph() == null || c.getGlyph().isBlank()) {
            c.setGlyph("✦");
        }
        if (c.getRankScore() == null) {
            c.setRankScore(0.0);
        }
        c.setHot(c.getHot() != null && c.getHot() == 1 ? 1 : 0);
    }

    private String validate(WorldCharacter c) {
        String err = Validators.text(c.getName(), "人物姓名", 50);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(c.getGrade(), "定位分级", GRADES);
        if (err != null) {
            return err;
        }
        err = Validators.range(c.getRankScore(), 0, RANK_MAX, "实力评分");
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getTitle(), "称号", 60);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getGlyph(), "卡面图标", 8);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getAvatar(), "头像图片路径", 255);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getRealm(), "当前境界", 60);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getFaction(), "出身势力", 60);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(c.getQuote(), "经典语录", 200);
        if (err != null) {
            return err;
        }
        return Validators.optionalText(c.getDescription(), "人物简介", 600);
    }

    private boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        var q = Wrappers.<WorldCharacter>lambdaQuery().eq(WorldCharacter::getName, name);
        if (excludeId != null) {
            q.ne(WorldCharacter::getId, excludeId);
        }
        return characterService.count(q) > 0;
    }
}
