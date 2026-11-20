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
 * @description 折扣：表达式为折扣率，取值范围 (0, 1]，例如 0.8 表示八折
 * @create 2026/11/16 21:42
 */
@Slf4j
@Service("ZK")
public class ZKCalculateService extends AbstractDiscountCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscount groupBuyDiscount) {
        BigDecimal rate = parseNonNegative(groupBuyDiscount.getMarketExpr());
        if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            log.error("折扣率超出范围(0,1] marketExpr:{}", groupBuyDiscount.getMarketExpr());
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        // 精度与最低价兜底由模板统一处理
        return originalPrice.multiply(rate);
    }

}
