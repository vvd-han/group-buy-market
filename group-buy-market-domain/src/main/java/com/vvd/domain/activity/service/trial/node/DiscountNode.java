package com.vvd.domain.activity.service.trial.node;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO.GroupBuyDiscount;
import com.vvd.domain.activity.service.discount.IDiscountCalculateService;
import com.vvd.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory.DynamicContext;
import com.vvd.types.design.framework.tree.StrategyHandler;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DiscountNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> {

    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Resource
    private EndNode endNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity request, DynamicContext context) throws Exception {
        GroupBuyDiscount discount = context.getGroupBuyActivityDiscountVO().getGroupBuyDiscount();
        if (discount == null) {
            throw new AppException(ResponseCode.E0002.getCode(), ResponseCode.E0002.getInfo());
        }
        IDiscountCalculateService calculator = discountCalculateServiceMap.get(discount.getMarketPlan());
        if (calculator == null) {
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());
        }
        context.setDeductionPrice(calculator.calculate(request.getUserId(), context.getSkuVO().getOriginalPrice(), discount));
        return router(request, context);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> get(
        MarketProductEntity request, DynamicContext context) {
        return endNode;
    }
}
