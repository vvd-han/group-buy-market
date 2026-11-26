package com.vvd.domain.trade.adapter.repository;

import com.vvd.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/26 21:19
 */
public interface ITradeRepository {

    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

}
