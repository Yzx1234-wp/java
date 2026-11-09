package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.RecordStatus;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.Validators;
import com.fenglin.springboottest.controller.dto.IdsRequest;
import com.fenglin.springboottest.entity.Quest;
import com.fenglin.springboottest.entity.QuestAccept;
import com.fenglin.springboottest.entity.User;
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
import java.util.Map;

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

    /** 管理员撤销某条接取记录（同时收回该记录已发放的积分） */
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

    /**
     * 后台点「完成」：把接取记录标记为已完成，并按任务当前积分发放积分。
     * <p>只有已完成的记录才计入修士总积分；重复点按会返回 409。</p>
     */
    @PostMapping("/accepts/{acceptId}/complete")
    @Transactional
    public Result<Map<String, Object>> completeAccept(@PathVariable Long acceptId,
                                                      HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        QuestAccept record = acceptMapper.selectById(acceptId);
        if (record == null) {
            return Result.fail(404, "接取记录不存在或已被删除");
        }
        if (RecordStatus.isCompleted(record.getStatus())) {
            return Result.fail(409, "该记录已完成，无需重复发放积分");
        }
        Quest quest = questService.getById(record.getQuestId());
        if (quest == null) {
            return Result.fail(404, "对应任务已被删除，无法发放积分");
        }
        int points = quest.getPoints() == null ? 0 : quest.getPoints();
        acceptMapper.update(null, Wrappers.<QuestAccept>lambdaUpdate()
                .eq(QuestAccept::getId, acceptId)
                .set(QuestAccept::getStatus, RecordStatus.COMPLETED)
                .set(QuestAccept::getPoints, points)
                .set(QuestAccept::getCompletedAt, LocalDateTime.now())
                .set(QuestAccept::getCompletedBy, operator(request)));
        return Result.ok(Map.of(
                "acceptId", acceptId,
                "username", record.getUsername(),
                "points", points,
                "status", RecordStatus.COMPLETED));
    }

    /** 后台撤销完成：回到「待完成」并收回该记录积分 */
    @PostMapping("/accepts/{acceptId}/revert")
    @Transactional
    public Result<Map<String, Object>> revertAccept(@PathVariable Long acceptId,
                                                    HttpServletRequest request) {
        Result<Void> auth = adminGuard.check(request);
        if (auth != null) {
            return Result.fail(auth.getCode(), auth.getMessage());
        }
        QuestAccept record = acceptMapper.selectById(acceptId);
        if (record == null) {
            return Result.fail(404, "接取记录不存在或已被删除");
        }
        if (!RecordStatus.isCompleted(record.getStatus())) {
            return Result.fail(409, "该记录尚未完成，无需撤销");
        }
        acceptMapper.update(null, Wrappers.<QuestAccept>lambdaUpdate()
                .eq(QuestAccept::getId, acceptId)
                .set(QuestAccept::getStatus, RecordStatus.PENDING)
                .set(QuestAccept::getCompletedAt, null)
                .set(QuestAccept::getCompletedBy, null));
        return Result.ok(Map.of(
                "acceptId", acceptId,
                "username", record.getUsername(),
                "withdrawn", record.getPoints() == null ? 0 : record.getPoints(),
                "status", RecordStatus.PENDING));
    }

    /** 当前操作的管理员账号（用于记录是谁点的完成） */
    private String operator(HttpServletRequest request) {
        User admin = adminGuard.currentAdmin(request);
        return admin == null ? "admin" : admin.getUsername();
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
