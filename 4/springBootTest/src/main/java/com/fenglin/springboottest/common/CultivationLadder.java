package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 修炼境界阶梯（《完美世界》十八境）与修为规则。
 * <p>境界<b>不由积分决定</b>：修为只来自「参悟功法」与「打坐吐纳」，
 * 积满本境所需修为后，需修士本人主动「冲击境界」渡劫，成功才晋升。</p>
 */
public final class CultivationLadder {

    /** 打坐吐纳冷却（秒） */
    public static final int MEDITATE_COOLDOWN = 30;

    /** 渡劫基础成功率（%） */
    public static final int BASE_RATE = 60;

    /** 每参悟一部功法的成功率加成（%） */
    public static final int RATE_PER_GONGFA = 2;

    /** 成功率上限（%） */
    public static final int MAX_RATE = 92;

    /** 失败后修为保留比例（%） */
    public static final int FAIL_KEEP = 75;

    /**
     * 境界节点。
     *
     * @param index 序号（0 起）
     * @param name  境界名
     * @param need  冲击下一境所需修为（最高境为 0）
     * @param tip   境界说明
     */
    public record Step(int index, String name, int need, String tip) {
    }

    private static final List<Step> STEPS = List.of(
            new Step(0, "搬血", 80, "淬炼气血，壮筋膜、强肉身，一切修行的起点"),
            new Step(1, "洞天", 120, "开辟洞天，吞吐天地灵气，可纳百川之力"),
            new Step(2, "化灵", 170, "化灵为神，神识初显，耳目可通百里"),
            new Step(3, "铭纹", 230, "身刻符文，举手投足皆成法则"),
            new Step(4, "列阵", 300, "以身为阵，攻守自成一界"),
            new Step(5, "尊者", 380, "一方尊者，镇压一地气运"),
            new Step(6, "神火", 470, "点燃神火，焚尽凡尘浊气"),
            new Step(7, "真一", 570, "万法归一，道心如铁"),
            new Step(8, "圣祭", 680, "以身为祭，换一线圣机"),
            new Step(9, "天神", 800, "登临神位，法则随心而动"),
            new Step(10, "虚道", 930, "体悟虚道，身与道合"),
            new Step(11, "斩我", 1070, "斩去旧我，另立真我"),
            new Step(12, "遁一", 1220, "遁去其一，留一线生机"),
            new Step(13, "至尊", 1380, "大荒至尊，一人镇万古"),
            new Step(14, "真仙", 1550, "超脱凡俗，位列真仙"),
            new Step(15, "仙王", 1730, "仙中之王，一念崩星河"),
            new Step(16, "准仙帝", 1920, "半步仙帝，只差最后一劫"),
            new Step(17, "仙帝", 0, "大荒之巅，独断万古")
    );

    private CultivationLadder() {
    }

    public static List<Step> steps() {
        return STEPS;
    }

    public static int size() {
        return STEPS.size();
    }

    /** 收敛到合法区间，防止历史脏数据越界 */
    public static int clampIndex(int index) {
        if (index < 0) {
            return 0;
        }
        return Math.min(index, STEPS.size() - 1);
    }

    public static Step step(int index) {
        return STEPS.get(clampIndex(index));
    }

    public static String name(int index) {
        return step(index).name();
    }

    /** 冲击下一境所需修为 */
    public static int need(int index) {
        return step(index).need();
    }

    public static String tip(int index) {
        return step(index).tip();
    }

    /** 是否已至最高境（仙帝） */
    public static boolean isTop(int index) {
        return clampIndex(index) >= STEPS.size() - 1;
    }

    /** 参悟品阶对应的修为收益 */
    public static int comprehendGain(String grade) {
        if (grade == null) {
            return 55;
        }
        return switch (grade) {
            case "绝世" -> 200;
            case "绝顶" -> 150;
            case "一流" -> 110;
            case "上乘" -> 80;
            default -> 55;
        };
    }

    /** 打坐吐纳一次的修为收益（境界越高，一次吐纳所得越多） */
    public static int meditateGain(int index) {
        return 24 + clampIndex(index) * 6;
    }

    /** 渡劫成功率：基础 60%，每参悟一部功法 +2%，上限 92% */
    public static int successRate(int comprehendCount) {
        return Math.min(MAX_RATE, BASE_RATE + Math.max(0, comprehendCount) * RATE_PER_GONGFA);
    }
}
