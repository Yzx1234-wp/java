package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.IdsRequest;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.mapper.QuestAcceptMapper;
import com.fenglin.springboottest.service.AdminGuard;
import com.fenglin.springboottest.service.QuestService;
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
 * 后台管理 · 大荒悬赏榜（含接取记录）。
 */
@RestController
@RequestMapping("/api/admin/quests")
public class AdminQuestController {

    private static final List<String> GRADES = List.of("仙帝", "仙王", "真仙", "上古");

    @Autowired
    private QuestService questService;

    @Autowired
    private QuestAcceptMapper acceptMapper;

    @Autowired
    private AdminGuard adminGuard;

    @GetMapping("/list")
    public Result<List<Quest>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(questService.search(keyword, grade, sortBy, null));
    }

    @PostMapping
    public Result<Quest> create(@RequestBody Quest body, HttpServletRequest request) {
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
            return Result.fail(409, "任务「" + body.getName() + "」已存在，请换一个");
        }
        body.setId(null);
        body.setCreatedAt(LocalDateTime.now());
        body.setAcceptedCount(null);
        body.setAccepted(null);
        questService.save(body);
        return Result.ok(questService.detail(body.getId(), null));
    }

    @PutMapping("/{id}")
    public Result<Quest> update(@PathVariable Long id, @RequestBody Quest body, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (questService.getById(id) == null) {
            return Result.fail(404, "未找到该任务，可能已被删除");
        }
        normalize(body);
        String err = validate(body);
        if (err != null) {
            return Result.fail(400, err);
        }
        if (nameExists(body.getName(), id)) {
            return Result.fail(409, "任务「" + body.getName() + "」已被其他记录占用");
        }
        body.setId(id);
        body.setCreatedAt(null);
        body.setAcceptedCount(null);
        body.setAccepted(null);
        questService.updateById(body);
        return Result.ok(questService.detail(id, null));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (questService.getById(id) == null) {
            return Result.fail(404, "未找到该任务，可能已被删除");
        }
        acceptMapper.delete(Wrappers.<QuestAccept>lambdaQuery().eq(QuestAccept::getQuestId, id));
        questService.removeById(id);
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
            return Result.fail(400, "请先勾选要删除的任务");
        }
        acceptMapper.delete(Wrappers.<QuestAccept>lambdaQuery().in(QuestAccept::getQuestId, body.getIds()));
        boolean ok = questService.removeByIds(body.getIds());
        return ok ? Result.ok(body.getIds().size()) : Result.fail(500, "批量删除失败，请重试");
    }

    @PostMapping("/{id}/toggle-hot")
    public Result<Integer> toggleHot(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        Quest old = questService.getById(id);
        if (old == null) {
            return Result.fail(404, "未找到该任务，可能已被删除");
        }
        Quest patch = new Quest();
        patch.setId(id);
        patch.setHot(old.getHot() != null && old.getHot() == 1 ? 0 : 1);
        questService.updateById(patch);
        return Result.ok(patch.getHot());
    }

    /** 某任务的接取记录 */
    @GetMapping("/{id}/accepts")
    public Result<List<QuestAccept>> accepts(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        return Result.ok(acceptMapper.selectList(Wrappers.<QuestAccept>lambdaQuery()
                .eq(QuestAccept::getQuestId, id)
                .orderByDesc(QuestAccept::getId)));
    }

    /** 管理员撤销某条接取记录（同时相当于收回该记录发放的积分） */
    @DeleteMapping("/accepts/{acceptId}")
    public Result<Void> deleteAccept(@PathVariable Long acceptId, HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        if (acceptMapper.selectById(acceptId) == null) {
            return Result.fail(404, "接取记录不存在或已被删除");
        }
        acceptMapper.deleteById(acceptId);
        return Result.ok();
    }

    // ---------- 内部工具 ----------

    private void normalize(Quest q) {
        if (q.getName() != null) {
            q.setName(q.getName().trim());
        }
        if (q.getGrade() != null) {
            q.setGrade(q.getGrade().trim());
        }
        if (q.getGlyph() == null || q.getGlyph().isBlank()) {
            q.setGlyph("⚔");
        }
        if (q.getPoints() == null || q.getPoints() < 0) {
            q.setPoints(0);
        }
        q.setHot(q.getHot() != null && q.getHot() == 1 ? 1 : 0);
    }

    private String validate(Quest q) {
        String err = Validators.text(q.getName(), "任务名称", 50);
        if (err != null) {
            return err;
        }
        err = Validators.oneOf(q.getGrade(), "任务等级", GRADES);
        if (err != null) {
            return err;
        }
        err = Validators.range(q.getPoints(), 0, 100000, "接取积分");
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(q.getGlyph(), "卡面图标", 8);
        if (err != null) {
            return err;
        }
        err = Validators.optionalText(q.getPoster(), "发布方", 60);
        if (err != null) {
            return err;
        }
        return Validators.optionalText(q.getDescription(), "任务简介", 600);
    }

    private boolean nameExists(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        var q = Wrappers.<Quest>lambdaQuery().eq(Quest::getName, name);
        if (excludeId != null) {
            q.ne(Quest::getId, excludeId);
        }
        return questService.count(q) > 0;
    }
}
