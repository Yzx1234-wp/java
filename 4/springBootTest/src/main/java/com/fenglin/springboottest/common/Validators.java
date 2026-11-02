package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 后台表单校验小工具：返回 null 表示通过，否则返回给前端展示的中文提示。
 */
public final class Validators {

    private Validators() {
    }

    /** 必填文本 + 最大长度 */
    public static String text(String value, String label, int max) {
        if (value == null || value.trim().isEmpty()) {
            return label + "不能为空";
        }
        if (value.trim().length() > max) {
            return label + "不能超过 " + max + " 个字符";
        }
        return null;
    }

    /** 选填文本 + 最大长度 */
    public static String optionalText(String value, String label, int max) {
        if (value != null && value.length() > max) {
            return label + "不能超过 " + max + " 个字符";
        }
        return null;
    }

    /** 必填且必须命中候选值 */
    public static String oneOf(String value, String label, List<String> allowed) {
        if (value == null || value.trim().isEmpty()) {
            return label + "不能为空";
        }
        if (!allowed.contains(value.trim())) {
            return label + "只能是：" + String.join(" / ", allowed);
        }
        return null;
    }

    /** 小数区间（允许为空） */
    public static String range(Double value, double min, double max, String label) {
        if (value == null) {
            return null;
        }
        if (value < min || value > max) {
            return label + "需要在 " + trim(min) + "-" + trim(max) + " 之间";
        }
        return null;
    }

    /** 整数区间（允许为空） */
    public static String range(Integer value, int min, int max, String label) {
        if (value == null) {
            return null;
        }
        if (value < min || value > max) {
            return label + "需要在 " + min + "-" + max + " 之间";
        }
        return null;
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
