package com.fenglin.springboottest.common;

/**
 * 接取 / 报名记录的完成状态。
 * <p>新规则：接取任务、报名秘境后为「待完成」，需后台管理点「完成」才发放积分。</p>
 */
public final class RecordStatus {

    /** 待完成（尚未发放积分） */
    public static final String PENDING = "PENDING";

    /** 已完成（后台已确认，积分已发放） */
    public static final String COMPLETED = "COMPLETED";

    private RecordStatus() {
    }

    /** 是否已完成 */
    public static boolean isCompleted(String status) {
        return COMPLETED.equalsIgnoreCase(status == null ? "" : status.trim());
    }

    /** 归一化：非 COMPLETED 一律视为待完成 */
    public static String normalize(String status) {
        return isCompleted(status) ? COMPLETED : PENDING;
    }
}
