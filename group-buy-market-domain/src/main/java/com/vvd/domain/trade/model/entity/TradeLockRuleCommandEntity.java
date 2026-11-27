package com.vvd.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hanjiazhi
 * @description 拼团交易锁单规则命令实体（责任链入参 T）
 * @create 2026/11/27 18:53
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleCommandEntity {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 活动ID
     */
    private Long activityId;

}
