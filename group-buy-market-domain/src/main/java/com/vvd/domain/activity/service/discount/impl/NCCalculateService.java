package com.vvd.domain.activity.service.discount.impl;

import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO.GroupBuyDiscount;
import com.vvd.domain.activity.service.discount.AbstractDiscountCalculateService;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description N元购：表达式为固定成交价，必须大于 0；高于原价时由模板兜底为原价
 * @create 2026/11/16 21:42
 */
@Slf4j
@Service("NC")
public class NCCalculateService extends AbstractDiscountCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscount groupBuyDiscount) {
        BigDecimal price = parseNonNegative(groupBuyDiscount.getMarketExpr());
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("N元购价格必须大于0 marketExpr:{}", groupBuyDiscount.getMarketExpr());
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        return price;
    }

}
