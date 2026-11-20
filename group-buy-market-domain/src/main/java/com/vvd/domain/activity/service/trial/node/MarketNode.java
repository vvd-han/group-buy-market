package com.vvd.domain.activity.service.trial.node;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.vvd.domain.activity.model.valobj.SkuVO;
import com.vvd.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.vvd.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import com.vvd.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import com.vvd.types.design.framework.tree.StrategyHandler;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/3 17:24
 */
@Service
public class MarketNode extends
    AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ErrorNode errorNode;

    @Resource
    private TagNode tagNode;

    @Override
    protected void multiThread(MarketProductEntity requestParameter,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext)
        throws ExecutionException, InterruptedException, TimeoutException {
        FutureTask<GroupBuyActivityDiscountVO> activityTask = new FutureTask<>(
            new QueryGroupBuyActivityDiscountVOThreadTask(
                requestParameter.getSource(), requestParameter.getChannel(), requestParameter.getGoodsId(), repository));
        FutureTask<SkuVO> skuTask = new FutureTask<>(
            new QuerySkuVOFromDBThreadTask(
                requestParameter.getSource(), requestParameter.getChannel(), requestParameter.getGoodsId(), repository));

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        try {
            threadPoolExecutor.execute(activityTask);
            threadPoolExecutor.execute(skuTask);
            dynamicContext.setGroupBuyActivityDiscountVO(
                activityTask.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS));
            dynamicContext.setSkuVO(skuTask.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS));
        } catch (TimeoutException | RejectedExecutionException e) {
            throw new AppException(ResponseCode.E0006.getCode(), ResponseCode.E0006.getInfo(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AppException) {
                throw (AppException) e.getCause();
            }
            throw e;
        } finally {
            if (activityTask.cancel(true)) {
                threadPoolExecutor.remove(activityTask);
            }
            if (skuTask.cancel(true)) {
                threadPoolExecutor.remove(skuTask);
            }
        }
    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityDiscountVO activity = dynamicContext.getGroupBuyActivityDiscountVO();
        if (activity != null && dynamicContext.getSkuVO() != null) {
            validateActivity(activity, new Date());
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(
        MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)
        throws Exception {
        if (dynamicContext.getGroupBuyActivityDiscountVO() == null || dynamicContext.getSkuVO() == null) {
            return errorNode;
        }
        return tagNode;
    }

    private void validateActivity(GroupBuyActivityDiscountVO activity, Date now) {
        Date start = activity.getStartTime();
        Date end = activity.getEndTime();
        if (start == null || end == null || !start.before(end)
            || activity.getTakeLimitCount() == null || activity.getTakeLimitCount() < 1
            || activity.getTarget() == null || activity.getTarget() < 1
            || activity.getValidTime() == null || activity.getValidTime() < 1) {
            throw new AppException(ResponseCode.E0007.getCode(), ResponseCode.E0007.getInfo());
        }
        if (now.before(start) || now.after(end)) {
            throw new AppException(ResponseCode.E0003.getCode(), ResponseCode.E0003.getInfo());
        }
    }

}
