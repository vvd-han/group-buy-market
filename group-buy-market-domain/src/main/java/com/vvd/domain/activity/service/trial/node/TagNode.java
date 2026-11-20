package com.vvd.domain.activity.service.trial.node;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.vvd.domain.activity.model.valobj.TagScopeEnumVO;
import com.vvd.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory.DynamicContext;
import com.vvd.types.design.framework.tree.StrategyHandler;
import java.util.EnumSet;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 人群标签节点：根据 tagScope 限制计算用户是否可见、是否可参与
 * @create 2026/11/20 15:34
 */
@Slf4j
@Service
public class TagNode extends
    AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> {

    @Resource
    private DiscountNode discountNode;

    @Resource
    private EndNode endNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DynamicContext dynamicContext)
        throws Exception {
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();

        EnumSet<TagScopeEnumVO> scopes = TagScopeEnumVO.parse(groupBuyActivityDiscountVO.getTagScope());
        String tagId = groupBuyActivityDiscountVO.getTagId();
        boolean matched = false;
        if (!scopes.isEmpty()) {
            if (StringUtils.isBlank(tagId)) {
                log.warn("活动人群限制缺少标签 activityId:{}", groupBuyActivityDiscountVO.getActivityId());
            } else {
                matched = repository.isTagCrowdRange(tagId, requestParameter.getUserId());
            }
        }
        boolean visible = !scopes.contains(TagScopeEnumVO.VISIBLE) || matched;
        dynamicContext.setVisible(visible);
        dynamicContext.setEnable(visible && (!scopes.contains(TagScopeEnumVO.ENABLE) || matched));

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> get(
        MarketProductEntity requestParameter, DynamicContext dynamicContext) throws Exception {
        return dynamicContext.isVisible() ? discountNode : endNode;
    }

}
