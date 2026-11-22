package com.vvd.domain.activity.service.trial.node;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory.DynamicContext;
import com.vvd.types.design.framework.tree.StrategyHandler;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/3 17:20
 */
@Slf4j
@Service
public class SwitchNode extends
    AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> {

    @Resource
    private MarketNode marketNode;

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

        String userId = requestParameter.getUserId();
        if (repository.downgradeSwitch()) {
            throw new AppException(ResponseCode.E0009.getCode(), ResponseCode.E0009.getInfo());
        }

        if (!repository.cutRange(userId)) {
            throw new AppException(ResponseCode.E0010.getCode(), ResponseCode.E0010.getInfo());
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> get(
        MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)
        throws Exception {
        return marketNode;
    }

}
