package com.vvd.infrastructure.adapter.repository;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.activity.model.valobj.DiscountTypeEnum;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.vvd.domain.activity.model.valobj.SCSkuActivityVO;
import com.vvd.domain.activity.model.valobj.SkuVO;
import com.vvd.infrastructure.dao.IGroupBuyActivityDao;
import com.vvd.infrastructure.dao.IGroupBuyDiscountDao;
import com.vvd.infrastructure.dao.ISCSkuActivityDao;
import com.vvd.infrastructure.dao.ISkuDao;
import com.vvd.infrastructure.dao.po.GroupBuyActivity;
import com.vvd.infrastructure.dao.po.GroupBuyDiscount;
import com.vvd.infrastructure.dao.po.SCSkuActivity;
import com.vvd.infrastructure.dao.po.Sku;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/11 15:59
 */
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Resource
    private ISkuDao skuDao;

    @Resource
    private ISCSkuActivityDao skuActivityDao;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
        GroupBuyActivity groupBuyActivityRes = groupBuyActivityDao.queryValidGroupBuyActivityId(activityId);
        if (null == groupBuyActivityRes) {
            return null;
        }

        String discountId = groupBuyActivityRes.getDiscountId();

        GroupBuyDiscount groupBuyDiscountRes = groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(
            discountId);
        if (null == groupBuyDiscountRes) {
            return null;
        }

        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
            .discountName(groupBuyDiscountRes.getDiscountName())
            .discountDesc(groupBuyDiscountRes.getDiscountDesc())
            .discountType(DiscountTypeEnum.get(groupBuyDiscountRes.getDiscountType()))
            .marketPlan(groupBuyDiscountRes.getMarketPlan())
            .marketExpr(groupBuyDiscountRes.getMarketExpr())
            .tagId(groupBuyDiscountRes.getTagId())
            .build();

        return GroupBuyActivityDiscountVO.builder()
            .activityId(groupBuyActivityRes.getActivityId())
            .activityName(groupBuyActivityRes.getActivityName())
            .groupBuyDiscount(groupBuyDiscount)
            .groupType(groupBuyActivityRes.getGroupType())
            .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
            .target(groupBuyActivityRes.getTarget())
            .validTime(groupBuyActivityRes.getValidTime())
            .status(groupBuyActivityRes.getStatus())
            .startTime(groupBuyActivityRes.getStartTime())
            .endTime(groupBuyActivityRes.getEndTime())
            .tagId(groupBuyActivityRes.getTagId())
            .tagScope(groupBuyActivityRes.getTagScope())
            .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (null == sku) {
            return null;
        }
        return SkuVO.builder()
            .goodsId(sku.getGoodsId())
            .goodsName(sku.getGoodsName())
            .originalPrice(sku.getOriginalPrice())
            .build();
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {
        SCSkuActivity scSkuActivityReq = new SCSkuActivity();
        scSkuActivityReq.setSource(source);
        scSkuActivityReq.setChannel(channel);
        scSkuActivityReq.setGoodsId(goodsId);

        SCSkuActivity scSkuActivity = skuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if (null == scSkuActivity) {
            return null;
        }

        return SCSkuActivityVO.builder()
            .source(scSkuActivity.getSource())
            .chanel(scSkuActivity.getChannel())
            .activityId(scSkuActivity.getActivityId())
            .goodsId(scSkuActivity.getGoodsId())
            .build();
    }

}
