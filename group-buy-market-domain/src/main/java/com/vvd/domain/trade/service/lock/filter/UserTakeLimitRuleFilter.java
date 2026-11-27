package com.vvd.domain.trade.service.lock.filter;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.vvd.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.vvd.types.design.framework.link.model2.handler.ILogicHandler;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 用户参与次数限制规则过滤节点（校验单用户是否达到活动限购上限）
 * @create 2026/11/27 20:21
 */
@Slf4j
@Service
public class UserTakeLimitRuleFilter implements
    ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    /**
     * 校验用户在该活动下的已参与次数是否超过限购上限，通过后返回已参与次数并结束责任链
     *
     * @param requestParameter 锁单规则命令入参
     * @param dynamicContext   责任链动态上下文（包含上游查出的活动信息）
     * @return 非 null 返回值，携带用户已参与次数并终止责任链流转
     */
    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter,
        TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-用户参与次数校验 {} activityId:{}", requestParameter.getUserId(),
            requestParameter.getActivityId());

        // 直接复用上游节点放入上下文的活动信息，减少一次数据库查询
        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();

        // 统计用户在当前活动下历史参与过的拼单次数
        Integer count = repository.queryOrderCountByActivityId(
            requestParameter.getActivityId(), requestParameter.getUserId());
        if (null == count) {
            count = 0;
        }

        // 若活动配置了单用户参与上限，且当前已参与次数达到或超过上限，则拦截锁单
        if (null != groupBuyActivity.getTakeLimitCount() && count >= groupBuyActivity.getTakeLimitCount()) {
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);
        }

        // 作为当前链尾节点，返回非 null 结果对象结束责任链；count 用于后续生成防刷唯一键 biz_id
        return TradeLockRuleFilterBackEntity.builder()
            .userTakeOrderCount(count)
            .build();
    }

}
