package com.vvd.domain.activity.service.discount;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.activity.model.valobj.DiscountTypeEnum;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO.GroupBuyDiscount;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author hanjiazhi
 * @description 折扣计算模板：人群过滤 -> 具体策略计算 -> 统一价格兜底（不高于原价、不低于最低价、保留两位小数）
 * @create 2026/11/16 21:17
 */
@Slf4j
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService {

    /**
     * 最低支付金额
     */
    protected static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    /**
     * 金额精度
     */
    protected static final int PRICE_SCALE = 2;

    private static final Pattern DECIMAL = Pattern.compile("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)");

    @Resource
    protected IActivityRepository repository;

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyDiscount groupBuyDiscount) {
        if (originalPrice == null || originalPrice.compareTo(MIN_PRICE) < 0 || originalPrice.stripTrailingZeros().scale() > PRICE_SCALE
            || groupBuyDiscount == null || groupBuyDiscount.getDiscountType() == null) {
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        BigDecimal price = originalPrice.setScale(PRICE_SCALE, RoundingMode.UNNECESSARY);

        // 人群标签优惠，不在人群范围内则按原价
        if (DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())) {
            boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
            if (!isCrowdRange) {
                return price;
            }
        }

        if (StringUtils.isBlank(groupBuyDiscount.getMarketExpr())) {
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        BigDecimal deductionPrice = doCalculate(price, groupBuyDiscount);
        if (deductionPrice == null) {
            throw new AppException(ResponseCode.E0005.getCode(), ResponseCode.E0005.getInfo());
        }
        return deductionPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP).max(MIN_PRICE).min(price);
    }

    /**
     * 人群标签过滤：标签ID为空视为配置缺失，不给予标签专属优惠
     */
    private boolean filterTagId(String userId, String tagId) {
        if (StringUtils.isBlank(tagId)) {
            log.warn("人群标签优惠未配置 tagId，按原价处理 userId:{}", userId);
            return false;
        }
        return repository.isTagCrowdRange(tagId, userId);
    }

    protected BigDecimal parseNonNegative(String value) {
        String decimal = value.trim();
        if (decimal.length() > 32 || !DECIMAL.matcher(decimal).matches()) {
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }
        return new BigDecimal(decimal);
    }

    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscount groupBuyDiscount);

}
