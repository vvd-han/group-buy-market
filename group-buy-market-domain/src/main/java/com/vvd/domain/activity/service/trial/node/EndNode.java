package com.vvd.domain.activity.service.trial.node;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.vvd.domain.activity.model.valobj.SkuVO;
import com.vvd.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory.DynamicContext;
import com.vvd.types.design.framework.tree.StrategyHandler;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/3 17:25
 */
@Slf4j
@Service
public class EndNode extends
    AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> {

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DynamicContext dynamicContext)
        throws Exception {
        if (!dynamicContext.isVisible()) {
            return TrialBalanceEntity.builder().isVisible(false).isEnable(false).build();
        }
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        SkuVO skuVO = dynamicContext.getSkuVO();

        BigDecimal deductionPrice = dynamicContext.getDeductionPrice();

        if (deductionPrice == null) {
            throw new AppException(ResponseCode.E0005.getCode(), ResponseCode.E0005.getInfo());
        }
        return TrialBalanceEntity.builder()
            .goodsId(skuVO.getGoodsId())
            .goodsName(skuVO.getGoodsName())
            .originalPrice(skuVO.getOriginalPrice())
            .deductionPrice(deductionPrice)
            .targetCount(groupBuyActivityDiscountVO.getTarget())
            .startTime(groupBuyActivityDiscountVO.getStartTime())
            .endTime(groupBuyActivityDiscountVO.getEndTime())
            .isVisible(dynamicContext.isVisible())
            .isEnable(dynamicContext.isEnable())
            .build();
    }

    @Override
    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> get(
        MarketProductEntity requestParameter, DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
