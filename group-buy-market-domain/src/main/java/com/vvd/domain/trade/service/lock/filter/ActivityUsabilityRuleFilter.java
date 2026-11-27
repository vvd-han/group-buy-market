package com.vvd.domain.trade.service.lock.filter;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.vvd.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.vvd.types.design.framework.link.model2.handler.ILogicHandler;
import com.vvd.types.enums.ActivityStatusEnumVO;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.Date;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 活动可用性规则过滤节点（校验活动存在性、生效状态与有效时间窗口）
 * @create 2026/11/27 20:21
 */
@Slf4j
@Service
public class ActivityUsabilityRuleFilter implements
    ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    /**
     * 执行活动可用性校验，通过后将活动实体写入上下文并放行至下一节点
     *
     * @param requestParameter 锁单规则命令入参（含 userId、activityId）
     * @param dynamicContext   责任链动态上下文
     * @return 返回 null 表示当前节点放行，继续执行下一节点；校验失败则抛出 AppException 中断链路
     */
    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter,
        TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-活动的可用性校验 {} activityId:{}", requestParameter.getUserId(),
            requestParameter.getActivityId());

        // 查询拼团活动配置
        GroupBuyActivityEntity groupBuyActivity = repository.queryGroupBuyActivityEntityByActivityId(
            requestParameter.getActivityId());

        // 兜底校验：防止传入不存在的活动ID导致后续空指针
        if (null == groupBuyActivity) {
            log.error("活动的可用性校验，拼团活动不存在 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0002);
        }

        // 校验活动状态：仅允许处于“生效”状态的活动参与锁单
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivity.getStatus())) {
            log.info("活动的可用性校验，非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101);
        }

        // 校验活动时间窗口：当前时间必须在活动开始与结束时间之间
        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivity.getStartTime()) || currentTime.after(groupBuyActivity.getEndTime())) {
            log.info("活动的可用性校验，非可参与时间范围 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102);
        }

        // 将活动实体写入上下文，供下游 UserTakeLimitRuleFilter 读取限购次数，避免重复查库
        dynamicContext.setGroupBuyActivity(groupBuyActivity);

        // 返回 null 通知责任链继续执行下一个过滤节点
        return next(requestParameter, dynamicContext);
    }

}
