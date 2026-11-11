package com.vvd.domain.activity.service.trial;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.vvd.types.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.vvd.types.design.framework.tree.AbstractStrategyRouter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.Resource;

/**
 * @author hanjiazhi
 * @description 抽象的拼团营销支撑类
 * @create 2026/11/3 17:11
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends
    AbstractMultiThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    protected long timeout = 500;

    @Resource
    protected IActivityRepository repository;

    @Override
    protected void multiThread(MarketProductEntity requestParameter,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext)
        throws ExecutionException, InterruptedException, TimeoutException {
        // 缺省的方法
    }

}
