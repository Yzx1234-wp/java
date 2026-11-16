package com.fenglin.springboottest.service;

import com.fenglin.springboottest.controller.dto.CultivationVO;
import com.fenglin.springboottest.entity.CultivationLog;

import java.util.List;
import java.util.Map;

/**
 * 「修炼境界」业务接口。
 * <p>境界不靠积分上涨：修为只来自「参悟功法」与「打坐吐纳」，
 * 修为积满后须本人主动「冲击境界」渡劫，成功才晋升，失败折损修为。</p>
 */
public interface CultivationService {

    /** 修炼总览：当前境界 / 修为进度 / 渡劫成功率 / 十八境阶梯 / 可参悟典籍 */
    CultivationVO overview(String username);

    /** 参悟一部功法（每部仅一次），返回本次所得修为与最新进度 */
    Map<String, Object> comprehend(String username, Long gongfaId);

    /** 打坐吐纳（有冷却），返回本次所得修为与最新进度 */
    Map<String, Object> meditate(String username);

    /** 冲击境界（渡劫）：成功晋升下一境，失败折损修为 */
    Map<String, Object> breakthrough(String username);

    /** 修炼记录（参悟 / 吐纳 / 突破 / 失败） */
    List<CultivationLog> logs(String username, int limit);
}
