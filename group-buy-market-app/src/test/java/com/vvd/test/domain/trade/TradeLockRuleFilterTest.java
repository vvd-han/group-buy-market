package com.vvd.test.domain.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.vvd.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.vvd.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.vvd.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.vvd.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import com.vvd.types.design.framework.link.model2.chain.BusinessLinkedList;
import com.vvd.types.enums.ActivityStatusEnumVO;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author hanjiazhi
 * @description 拼团锁单规则责任链单元测试（覆盖正常放行及各类拦截场景）
 * @create 2026/11/27 22:23
 */
public class TradeLockRuleFilterTest {

    private ITradeRepository repository;

    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter;

    @Before
    public void setUp() {
        repository = mock(ITradeRepository.class);

        // 构造责任链节点并注入 Mock 仓储，纯内存验证业务规则，不依赖外部数据库
        ActivityUsabilityRuleFilter activityFilter = new ActivityUsabilityRuleFilter();
        UserTakeLimitRuleFilter userLimitFilter = new UserTakeLimitRuleFilter();
        ReflectionTestUtils.setField(activityFilter, "repository", repository);
        ReflectionTestUtils.setField(userLimitFilter, "repository", repository);

        TradeLockRuleFilterFactory factory = new TradeLockRuleFilterFactory();
        tradeRuleFilter = factory.tradeRuleFilter(activityFilter, userLimitFilter);
    }

    /**
     * 正常场景：活动生效、在有效期内、用户未达限购上限 -> 放行并返回用户历史参与次数
     */
    @Test
    public void test_apply_success() throws Exception {
        Long activityId = 100123L;
        String userId = "vvd_user_01";
        when(repository.queryGroupBuyActivityEntityByActivityId(activityId))
            .thenReturn(buildActivity(activityId, ActivityStatusEnumVO.EFFECTIVE, -60_000L, 60_000L, 2));
        when(repository.queryOrderCountByActivityId(activityId, userId)).thenReturn(1);

        TradeLockRuleFilterBackEntity back = tradeRuleFilter.apply(
            TradeLockRuleCommandEntity.builder().activityId(activityId).userId(userId).build(),
            new TradeLockRuleFilterFactory.DynamicContext());

        assertNotNull(back);
        assertEquals(Integer.valueOf(1), back.getUserTakeOrderCount());
    }

    /**
     * 异常场景：活动不存在 -> 拦截并抛出 E0002，且不触发下游限购查询
     */
    @Test
    public void test_apply_activity_not_found() throws Exception {
        Long activityId = 999999L;
        String userId = "vvd_user_01";
        when(repository.queryGroupBuyActivityEntityByActivityId(activityId)).thenReturn(null);

        try {
            tradeRuleFilter.apply(
                TradeLockRuleCommandEntity.builder().activityId(activityId).userId(userId).build(),
                new TradeLockRuleFilterFactory.DynamicContext());
            fail("预期应抛出活动不存在异常 E0002");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0002.getCode(), e.getCode());
            verify(repository, never()).queryOrderCountByActivityId(activityId, userId);
        }
    }

    /**
     * 异常场景：活动状态为非生效（如创建/废弃） -> 拦截并抛出 E0101
     */
    @Test
    public void test_apply_activity_not_effective() throws Exception {
        Long activityId = 100123L;
        String userId = "vvd_user_01";
        when(repository.queryGroupBuyActivityEntityByActivityId(activityId))
            .thenReturn(buildActivity(activityId, ActivityStatusEnumVO.CREATE, -60_000L, 60_000L, 1));

        try {
            tradeRuleFilter.apply(
                TradeLockRuleCommandEntity.builder().activityId(activityId).userId(userId).build(),
                new TradeLockRuleFilterFactory.DynamicContext());
            fail("预期应抛出活动未生效异常 E0101");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0101.getCode(), e.getCode());
            verify(repository, never()).queryOrderCountByActivityId(activityId, userId);
        }
    }

    /**
     * 异常场景：当前时间超出活动结束时间 -> 拦截并抛出 E0102
     */
    @Test
    public void test_apply_activity_expired() throws Exception {
        Long activityId = 100123L;
        String userId = "vvd_user_01";
        when(repository.queryGroupBuyActivityEntityByActivityId(activityId))
            .thenReturn(buildActivity(activityId, ActivityStatusEnumVO.EFFECTIVE, -120_000L, -60_000L, 1));

        try {
            tradeRuleFilter.apply(
                TradeLockRuleCommandEntity.builder().activityId(activityId).userId(userId).build(),
                new TradeLockRuleFilterFactory.DynamicContext());
            fail("预期应抛出活动不在有效时间范围内异常 E0102");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0102.getCode(), e.getCode());
            verify(repository, never()).queryOrderCountByActivityId(activityId, userId);
        }
    }

    /**
     * 异常场景：用户已参与次数达到活动限购上限 -> 拦截并抛出 E0103
     */
    @Test
    public void test_apply_user_take_limit_reached() throws Exception {
        Long activityId = 100123L;
        String userId = "vvd_user_01";
        when(repository.queryGroupBuyActivityEntityByActivityId(activityId))
            .thenReturn(buildActivity(activityId, ActivityStatusEnumVO.EFFECTIVE, -60_000L, 60_000L, 1));
        when(repository.queryOrderCountByActivityId(activityId, userId)).thenReturn(1);

        try {
            tradeRuleFilter.apply(
                TradeLockRuleCommandEntity.builder().activityId(activityId).userId(userId).build(),
                new TradeLockRuleFilterFactory.DynamicContext());
            fail("预期应抛出参与次数已达上限异常 E0103");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0103.getCode(), e.getCode());
        }
    }

    private GroupBuyActivityEntity buildActivity(Long activityId, ActivityStatusEnumVO status,
        long startOffsetMs, long endOffsetMs, Integer takeLimitCount) {
        long now = System.currentTimeMillis();
        return GroupBuyActivityEntity.builder()
            .activityId(activityId)
            .activityName("测试拼团活动")
            .status(status)
            .startTime(new Date(now + startOffsetMs))
            .endTime(new Date(now + endOffsetMs))
            .takeLimitCount(takeLimitCount)
            .build();
    }

}
