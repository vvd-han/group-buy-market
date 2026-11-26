package com.vvd.domain.trade.service;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.entity.PayActivityEntity;
import com.vvd.domain.trade.model.entity.PayDiscountEntity;
import com.vvd.domain.trade.model.entity.UserEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/26 21:17
 */
@Slf4j
@Service
public class TradeOrderService implements ITradeOrderService {


    @Resource
    ITradeRepository repository;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        return repository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        return repository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,
        PayDiscountEntity payDiscountEntity) {

        GroupBuyOrderAggregate groupBuyOrderAggregate = GroupBuyOrderAggregate.builder()
            .userEntity(userEntity)
            .payActivityEntity(payActivityEntity)
            .payDiscountEntity(payDiscountEntity)
            .build();

        return repository.lockMarketPayOrder(groupBuyOrderAggregate);
    }

}
