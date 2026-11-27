package com.vvd.domain.trade.adapter.repository;

import com.vvd.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @author hanjiazhi
 * @description 拼团交易仓储服务接口（由基础设施层实现具体的数据存取）
 * @create 2026/11/26 21:19
 */
public interface ITradeRepository {

    /**
     * 根据用户ID和外部交易单号查询未支付的营销预购订单（用于锁单幂等校验）
     *
     * @param userId     用户ID
     * @param outTradeNo 外部唯一交易单号
     * @return 未支付的营销预购订单实体，不存在则返回 null
     */
    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    /**
     * 锁定拼团营销预支付订单（开团新建主单或参团累加锁单数，并写入用户拼单明细）
     *
     * @param groupBuyOrderAggregate 拼团订单聚合对象
     * @return 锁单成功后的营销预购订单实体
     */
    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    /**
     * 查询指定拼团队伍的当前进度（目标人数、完成人数、锁单人数）
     *
     * @param teamId 拼单组队ID
     * @return 拼团进度值对象
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    /**
     * 根据活动ID查询拼团活动配置实体（供锁单规则链校验活动状态与有效期）
     *
     * @param activityId 活动ID
     * @return 拼团活动实体，不存在则返回 null
     */
    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    /**
     * 查询指定用户在某个拼团活动下已参与的订单次数（用于单用户限购校验）
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 用户已参与次数
     */
    Integer queryOrderCountByActivityId(Long activityId, String userId);

}
