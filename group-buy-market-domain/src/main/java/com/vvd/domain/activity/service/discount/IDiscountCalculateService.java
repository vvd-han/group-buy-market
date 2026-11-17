package com.vvd.domain.activity.service.discount;

import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import java.math.BigDecimal;

public interface IDiscountCalculateService {

    BigDecimal calculate(String userId, BigDecimal originalPrice,
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);

}
