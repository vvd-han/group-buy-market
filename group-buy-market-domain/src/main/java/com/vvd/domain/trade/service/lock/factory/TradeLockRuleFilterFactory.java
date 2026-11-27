package com.vvd.domain.trade.service.lock.factory;

import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.vvd.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.vvd.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import com.vvd.types.design.framework.link.model2.LinkArmory;
import com.vvd.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 交易锁单规则过滤工厂（负责装配锁单责任链及定义节点共享上下文）
 * @create 2026/11/27 20:21
 */
@Slf4j
@Service
public class TradeLockRuleFilterFactory {

    /**
     * 装配锁单规则责任链并注册为 Spring Bean 执行顺序：1. 活动可用性校验 -> 2. 用户参与次数限制校验
     *
     * @param activityUsabilityRuleFilter 活动可用性规则过滤节点
     * @param userTakeLimitRuleFilter     用户限购次数规则过滤节点
     * @return 装配好的业务责任链对象
     */
    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter(
        ActivityUsabilityRuleFilter activityUsabilityRuleFilter,
        UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        // 按业务校验顺序串联节点：先确认活动有效，再校验用户限购，避免对无效活动多做一次用户统计查询
        LinkArmory<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> linkArmory =
            new LinkArmory<>("交易规则过滤链", activityUsabilityRuleFilter, userTakeLimitRuleFilter);

        return linkArmory.getLogicLink();
    }

    /**
     * @author hanjiazhi
     * @description 锁单责任链动态上下文（在同一次请求的不同过滤节点之间共享数据）
     * @create 2026/11/27 20:21
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /**
         * 拼团活动信息（由节点1查库后写入，供后续节点直接读取，避免重复查库）
         */
        private GroupBuyActivityEntity groupBuyActivity;

    }

}
