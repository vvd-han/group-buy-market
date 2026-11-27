package com.vvd.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hanjiazhi
 * @description 拼团交易锁单规则过滤返回实体（责任链返回值 R）
 * @create 2026/11/27 18:53
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleFilterBackEntity {

    /**
     * 用户已参与该活动的订单数量
     */
    private Integer userTakeOrderCount;

}
