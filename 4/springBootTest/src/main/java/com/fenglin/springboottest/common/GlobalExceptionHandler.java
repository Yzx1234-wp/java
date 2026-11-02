package com.fenglin.springboottest.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理：把数据库约束冲突、参数格式错误等底层异常
 * 统一翻译成前端可直接展示的中文提示，避免出现 Spring 默认的 500 白页
 * （例如「新增同名典籍」原先会直接返回 500 Internal Server Error）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Duplicate entry '原始真解' for key 'gongfa.uk_name' */
    private static final Pattern DUP = Pattern.compile("Duplicate entry '([^']*)' for key '([^']+)'");

    /** 唯一索引 → 中文字段名，用于把「重名」翻译成人话 */
    private static final Map<String, String> KEY_LABELS = new LinkedHashMap<>();

    static {
        KEY_LABELS.put("gongfa.uk_name", "功法名称");
        KEY_LABELS.put("world_character.uk_name", "人物姓名");
        KEY_LABELS.put("secret_realm.uk_name", "秘境名称");
        KEY_LABELS.put("quest.uk_name", "任务名称");
        KEY_LABELS.put("star_user.uk_username", "账号");
        KEY_LABELS.put("realm_signup.uk_realm_user", "报名记录");
        KEY_LABELS.put("quest_accept.uk_quest_user", "接取记录");
    }

    /** 唯一键冲突（重名 / 重复报名 / 重复接取） */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> onDuplicateKey(DuplicateKeyException e) {
        String raw = rootMessage(e);
        Matcher m = DUP.matcher(raw == null ? "" : raw);
        if (m.find()) {
            String value = m.group(1);
            String key = m.group(2);
            String label = KEY_LABELS.getOrDefault(key.toLowerCase(), "内容");
            if (key.toLowerCase().endsWith(".uk_realm_user")) {
                return Result.fail(409, "该账号已报名过此秘境，不能重复报名");
            }
            if (key.toLowerCase().endsWith(".uk_quest_user")) {
                return Result.fail(409, "该账号已接取过此项任务，不能重复接取");
            }
            return Result.fail(409, label + "「" + value + "」已存在，请换一个");
        }
        return Result.fail(409, "数据已存在，请勿重复提交");
    }

    /** 非空 / 长度 / 外键等约束不满足 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> onDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据约束校验失败: {}", rootMessage(e));
        return Result.fail(400, "数据不符合要求：请检查必填项是否为空、内容是否超出长度限制");
    }

    /** 请求体 JSON 解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> onUnreadable(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求内容格式有误，请刷新页面后重试");
    }

    /** 查询参数类型不匹配（如 id 传了非数字） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> onTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(400, "参数「" + e.getName() + "」格式不正确");
    }

    /** 缺少必填查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> onMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必要参数：" + e.getParameterName());
    }

    /** 请求方式不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> onMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(405, "接口不支持 " + e.getMethod() + " 请求");
    }

    /** 静态资源 / 接口不存在 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> onNotFound(NoResourceFoundException e) {
        return Result.fail(404, "接口不存在：" + e.getResourcePath());
    }

    /** 业务主动抛出的参数异常（如报名名额已满） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> onIllegalArgument(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage() == null ? "参数不合法" : e.getMessage());
    }

    /** 兜底：任何未处理异常都不再把堆栈抛给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> onException(Exception e) {
        log.error("未处理的服务器异常", e);
        return Result.fail(500, "服务器开小差了，请稍后重试（如持续失败请联系管理员）");
    }

    private String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            if (t.getMessage() != null && !t.getMessage().isBlank()) {
                return t.getMessage();
            }
            t = t.getCause();
        }
        return t.getMessage();
    }
}
