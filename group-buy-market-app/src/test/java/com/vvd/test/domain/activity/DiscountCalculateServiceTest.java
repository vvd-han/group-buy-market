package com.vvd.test.domain.activity;

import com.vvd.domain.activity.model.valobj.DiscountTypeEnum;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO.GroupBuyDiscount;
import com.vvd.domain.activity.service.discount.impl.MJCalculateService;
import com.vvd.domain.activity.service.discount.impl.NCCalculateService;
import com.vvd.domain.activity.service.discount.impl.ZJCalculateService;
import com.vvd.domain.activity.service.discount.impl.ZKCalculateService;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.*;

public class DiscountCalculateServiceTest {

    @Test
    public void roundsAndBoundsDiscountedPrices() {
        assertEquals(new BigDecimal("84.99"), new ZKCalculateService().calculate("user", new BigDecimal("99.99"), base("0.85")));
        assertEquals(new BigDecimal("3.10"), new NCCalculateService().calculate("user", new BigDecimal("3.1"), base("9")));
        assertEquals(new BigDecimal("0.01"), new ZJCalculateService().calculate("user", new BigDecimal("100"), base("100")));
    }

    @Test
    public void rejectsInvalidConfigAndSubCentOriginal() {
        assertEquals("E0004", assertThrows(AppException.class, () -> DiscountTypeEnum.get(null)).getCode());
        assertEquals("E0004", assertThrows(AppException.class, () -> DiscountTypeEnum.get(2)).getCode());
        BigDecimal price = new BigDecimal("100.00");
        assertThrows(AppException.class, () -> new MJCalculateService().calculate("user", price, base("100,20,")));
        assertThrows(AppException.class, () -> new ZJCalculateService().calculate("user", price, base("-1")));
        assertThrows(AppException.class, () -> new NCCalculateService().calculate("user", price, base("0")));
        assertThrows(AppException.class, () -> new ZKCalculateService().calculate("user", price, base("1.5")));
        assertThrows(AppException.class, () -> new ZKCalculateService().calculate("user", new BigDecimal("0.004"), base("0.5")));
    }

    private GroupBuyDiscount base(String expression) {
        return GroupBuyDiscount.builder().discountType(DiscountTypeEnum.BASE).marketExpr(expression).build();
    }
}
