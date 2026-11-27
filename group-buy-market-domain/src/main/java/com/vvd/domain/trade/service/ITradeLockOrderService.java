package com.vvd.domain.trade.service;

import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.entity.PayActivityEntity;
import com.vvd.domain.trade.model.entity.PayDiscountEntity;
import com.vvd.domain.trade.model.entity.UserEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @author hanjiazhi
 * @description 拼团交易锁单服务接口
 * @create 2026/11/25 21:08
 */
public interface ITradeLockOrderService {

    /**
     * 查询未被支付消费完成的营销优惠订单（用于锁单接口幂等返回）
     *
     * @param userId     用户ID
     * @param outTradeNo 外部唯一单号
     * @return 拼团预购订单营销实体对象
     */
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 查询拼团进度（目标人数、完成人数、已锁单人数）
     *
     * @param teamId 拼团组队ID
     * @return 拼团进度值对象
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    /**
     * 锁定营销预支付订单（先执行锁单规则责任链校验，通过后聚合落库）
     *
     * @param userEntity        用户实体对象
     * @param payActivityEntity 支付活动实体对象
     * @param payDiscountEntity 支付优惠实体对象
     * @return 锁单成功后的预购订单营销实体对象
     * @throws Exception 规则校验或执行过程中抛出的异常
     */
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,
        PayDiscountEntity payDiscountEntity) throws Exception;

}
