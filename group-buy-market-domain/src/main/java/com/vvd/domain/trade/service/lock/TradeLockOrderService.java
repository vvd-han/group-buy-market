package com.vvd.domain.trade.service.lock;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.entity.PayActivityEntity;
import com.vvd.domain.trade.model.entity.PayDiscountEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.vvd.domain.trade.model.entity.UserEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;
import com.vvd.domain.trade.service.ITradeLockOrderService;
import com.vvd.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.vvd.types.design.framework.link.model2.chain.BusinessLinkedList;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 拼团交易锁单服务实现（编排锁单规则责任链与订单聚合落库）
 * @create 2026/11/27 20:40
 */
@Slf4j
@Service
public class TradeLockOrderService implements ITradeLockOrderService {

    @Resource
    private ITradeRepository repository;

    /**
     * 交易锁单规则过滤责任链（由 TradeLockRuleFilterFactory 装配并注入）
     */
    @Resource
    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter;

    /**
     * 查询用户尚未支付完成的营销预购订单
     *
     * @param userId     用户ID
     * @param outTradeNo 外部唯一交易单号
     * @return 未支付订单实体，不存在则返回 null
     */
    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易-查询未支付营销订单:{} outTradeNo:{}", userId, outTradeNo);
        return repository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    /**
     * 查询指定拼团队伍的成团进度
     *
     * @param teamId 拼团组队ID
     * @return 拼团进度值对象
     */
    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("拼团交易-查询拼单进度:{}", teamId);
        return repository.queryGroupBuyProgress(teamId);
    }

    /**
     * 锁定拼团营销预支付订单：先过规则责任链校验，再组装聚合对象交由仓储层落库
     *
     * @param userEntity        用户实体对象
     * @param payActivityEntity 支付活动实体对象
     * @param payDiscountEntity 支付优惠实体对象
     * @return 锁单成功后的预购订单实体
     */
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,
        PayDiscountEntity payDiscountEntity) throws Exception {
        log.info("拼团交易-锁定营销优惠支付订单:{} activityId:{} goodsId:{}",
            userEntity.getUserId(), payActivityEntity.getActivityId(), payDiscountEntity.getGoodsId());

        // 1. 执行锁单规则责任链（依次校验活动可用性 -> 用户限购次数），不通过直接抛 AppException 拦截
        TradeLockRuleFilterBackEntity tradeLockRuleFilterBackEntity = tradeRuleFilter.apply(
            TradeLockRuleCommandEntity.builder()
                .activityId(payActivityEntity.getActivityId())
                .userId(userEntity.getUserId())
                .build(),
            new TradeLockRuleFilterFactory.DynamicContext());

        // 取出用户已参与次数，后续在仓储层用于拼接数据库唯一索引 biz_id，防并发超限下单
        Integer userTakeOrderCount = tradeLockRuleFilterBackEntity.getUserTakeOrderCount();

        // 2. 构建拼团订单聚合对象
        GroupBuyOrderAggregate groupBuyOrderAggregate = GroupBuyOrderAggregate.builder()
            .userEntity(userEntity)
            .payActivityEntity(payActivityEntity)
            .payDiscountEntity(payDiscountEntity)
            .userTakeOrderCount(userTakeOrderCount)
            .build();

        // 3. 调用仓储层落库锁单（此时仅占坑锁单尚未支付，后续将流转至支付结算或超时退单）
        return repository.lockMarketPayOrder(groupBuyOrderAggregate);
    }

}
