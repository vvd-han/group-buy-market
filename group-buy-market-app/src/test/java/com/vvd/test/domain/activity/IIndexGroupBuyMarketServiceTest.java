package com.vvd.test.domain.activity;

import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.service.IIndexGroupBuyMarketService;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IIndexGroupBuyMarketServiceTest {

    @Resource
    private IIndexGroupBuyMarketService service;

    @Resource
    private JdbcTemplate jdbc;

    @MockBean
    private RedissonClient redis;

    @Test
    public void scopesActivityAndSkuAndChecksExactTagMembership() throws Exception {
        TrialBalanceEntity firstChannel = trial("s01", "c01", "outsider", "9890001");
        assertEquals(new BigDecimal("100.00"), firstChannel.getOriginalPrice());
        assertEquals(new BigDecimal("80.00"), firstChannel.getDeductionPrice());

        TrialBalanceEntity secondChannel = trial("s01", "c02", "xiaofuge", "9890001");
        assertEquals(new BigDecimal("99.99"), secondChannel.getOriginalPrice());
        assertEquals(new BigDecimal("84.99"), secondChannel.getDeductionPrice());
        assertEquals(new BigDecimal("99.99"), trial("s01", "c02", "XIAOFUGE", "9890001").getDeductionPrice());

        assertEquals("E0002", assertThrows(AppException.class,
            () -> trial("s01", "unlisted", "xiaofuge", "9890001")).getCode());
        assertEquals("E0002", assertThrows(AppException.class,
            () -> trial("other", "c01", "xiaofuge", "9890001")).getCode());
    }

    @Test
    public void enforcesActivityWindowAndTagScope() throws Exception {
        jdbc.update("insert into group_buy_activity (activity_id, activity_name, discount_id, status, start_time, end_time,"
            + " tag_id, tag_scope) values (990123, 'trial test', '25120207', 1,"
            + " now() - interval 2 day, now() - interval 1 day, 'DEMO_TAG', '1')");
        try {
            jdbc.update("insert into sku (source, channel, goods_id, goods_name, original_price)"
                + " values ('trial', 'test', 'hidden', 'Hidden item', 100)");
            jdbc.update("insert into sc_sku_activity (source, channel, goods_id, activity_id)"
                + " values ('trial', 'test', 'hidden', 990123)");

            assertEquals("E0003", assertThrows(AppException.class,
                () -> trial("trial", "test", "xiaofuge", "hidden")).getCode());
            jdbc.update("update group_buy_activity set start_time = now() + interval 1 day,"
                + " end_time = now() + interval 2 day where activity_id = 990123");
            assertEquals("E0003", assertThrows(AppException.class,
                () -> trial("trial", "test", "xiaofuge", "hidden")).getCode());

            jdbc.update("update group_buy_activity set start_time = now() - interval 1 day where activity_id = 990123");
            TrialBalanceEntity hidden = trial("trial", "test", "outsider", "hidden");
            assertEquals(Boolean.FALSE, hidden.getIsVisible());
            assertEquals(Boolean.FALSE, hidden.getIsEnable());
            assertNull(hidden.getGoodsId());
            assertNull(hidden.getGoodsName());
            assertNull(hidden.getDeductionPrice());
            assertNull(hidden.getStartTime());
            assertNull(hidden.getEndTime());
            assertEquals(new BigDecimal("80.00"), trial("trial", "test", "xiaofuge", "hidden").getDeductionPrice());

            jdbc.update("update group_buy_activity set tag_scope = '2', tag_id = null where activity_id = 990123");
            TrialBalanceEntity missingTag = trial("trial", "test", "outsider", "hidden");
            assertEquals(Boolean.TRUE, missingTag.getIsVisible());
            assertEquals(Boolean.FALSE, missingTag.getIsEnable());

            jdbc.update("update group_buy_activity set tag_scope = '9' where activity_id = 990123");
            assertEquals("E0007", assertThrows(AppException.class,
                () -> trial("trial", "test", "outsider", "hidden")).getCode());
        } finally {
            jdbc.update("delete from sc_sku_activity where activity_id = 990123");
            jdbc.update("delete from sku where source = 'trial' and channel = 'test' and goods_id = 'hidden'");
            jdbc.update("delete from group_buy_activity where activity_id = 990123");
        }
    }

    private TrialBalanceEntity trial(String source, String channel, String userId, String goodsId) throws Exception {
        return service.indexMarketTrial(MarketProductEntity.builder()
            .source(source).channel(channel).userId(userId).goodsId(goodsId).build());
    }
}
