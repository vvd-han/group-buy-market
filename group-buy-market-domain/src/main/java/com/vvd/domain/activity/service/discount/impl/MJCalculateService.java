package com.vvd.domain.activity.service.discount.impl;

import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO.GroupBuyDiscount;
import com.vvd.domain.activity.service.discount.AbstractDiscountCalculateService;
import com.vvd.types.common.Constants;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description 满减：表达式 "x,y" 表示满 x 元减 y 元
 * @create 2026/11/16 21:42
 */
@Slf4j
@Service("MJ")
public class MJCalculateService extends AbstractDiscountCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscount groupBuyDiscount) {
        String[] split = groupBuyDiscount.getMarketExpr().split(Constants.SPLIT, -1);
        if (split.length != 2) {
            log.error("满减表达式格式错误 marketExpr:{}", groupBuyDiscount.getMarketExpr());
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        BigDecimal x = parseNonNegative(split[0]);
        BigDecimal y = parseNonNegative(split[1]);

        // 未达到满减门槛，按原价
        if (originalPrice.compareTo(x) < 0) {
            return originalPrice;
        }

        // 最低价兜底由模板统一处理
        return originalPrice.subtract(y);
    }

}
