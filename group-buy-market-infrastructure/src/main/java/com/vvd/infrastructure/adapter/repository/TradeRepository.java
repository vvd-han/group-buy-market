package com.vvd.infrastructure.adapter.repository;

import com.vvd.domain.trade.adapter.repository.ITradeRepository;
import com.vvd.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.vvd.domain.trade.model.entity.GroupBuyActivityEntity;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.entity.PayActivityEntity;
import com.vvd.domain.trade.model.entity.PayDiscountEntity;
import com.vvd.domain.trade.model.entity.UserEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;
import com.vvd.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.vvd.infrastructure.dao.IGroupBuyActivityDao;
import com.vvd.infrastructure.dao.IGroupBuyOrderDao;
import com.vvd.infrastructure.dao.IGroupBuyOrderListDao;
import com.vvd.infrastructure.dao.po.GroupBuyActivity;
import com.vvd.infrastructure.dao.po.GroupBuyOrder;
import com.vvd.infrastructure.dao.po.GroupBuyOrderList;
import com.vvd.types.common.Constants;
import com.vvd.types.enums.ActivityStatusEnumVO;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/26 21:25
 */
@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;


    @Override
    public MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderListRes = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(
            groupBuyOrderListReq);
        if (null == groupBuyOrderListRes) {
            return null;
        }

        return MarketPayOrderEntity.builder()
            .orderId(groupBuyOrderListRes.getOrderId())
            .deductionPrice(groupBuyOrderListRes.getDeductionPrice())
            .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderListRes.getStatus()))
            .build();
    }


    @Transactional(timeout = 500)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {
        // 获取用户历史已参与次数，用于拼接本次订单的防刷唯一键 biz_id
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();
        if (null == userTakeOrderCount) {
            userTakeOrderCount = 0;
        }

        // 聚合对象信息
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();

        String teamId = payActivityEntity.getTeamId();

        if (StringUtils.isBlank(teamId)) {
            teamId = RandomStringUtils.randomNumeric(8);

            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                .teamId(teamId)
                .activityId(payActivityEntity.getActivityId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getDeductionPrice())
                .targetCount(payActivityEntity.getTargetCount())
                .completeCount(0)
                .lockCount(1)
                .build();

            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            int updateAddLockCount = groupBuyOrderDao.updateAddLockCount(teamId);
            if (1 != updateAddLockCount) {
                throw new AppException(ResponseCode.E0011);
            }
        }

        // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
            .userId(userEntity.getUserId())
            .teamId(teamId)
            .orderId(orderId)
            .activityId(payActivityEntity.getActivityId())
            .startTime(payActivityEntity.getStartTime())
            .endTime(payActivityEntity.getEndTime())
            .goodsId(payDiscountEntity.getGoodsId())
            .source(payDiscountEntity.getSource())
            .channel(payDiscountEntity.getChannel())
            .originalPrice(payDiscountEntity.getOriginalPrice())
            .deductionPrice(payDiscountEntity.getDeductionPrice())
            .status(TradeOrderStatusEnumVO.CREATE.getCode())
            .outTradeNo(payDiscountEntity.getOutTradeNo())
            .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId()
                + Constants.UNDERLINE + (userTakeOrderCount + 1))
            .build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        } catch (DuplicateKeyException e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderEntity.builder()
            .orderId(orderId)
            .deductionPrice(payDiscountEntity.getDeductionPrice())
            .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
            .build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {

        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);

        if (null == groupBuyOrder) {
            return null;
        }
        return GroupBuyProgressVO.builder()
            .targetCount(groupBuyOrder.getTargetCount())
            .completeCount(groupBuyOrder.getCompleteCount())
            .lockCount(groupBuyOrder.getLockCount())
            .build();
    }

    /**
     * 根据活动ID查询拼团活动实体，并将数据库的整型状态转换为领域状态枚举
     *
     * @param activityId 活动ID
     * @return 拼团活动领域实体，查无记录时返回 null
     */
    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        if (null == groupBuyActivity) {
            return null;
        }
        return GroupBuyActivityEntity.builder()
            .activityId(groupBuyActivity.getActivityId())
            .activityName(groupBuyActivity.getActivityName())
            .discountId(groupBuyActivity.getDiscountId())
            .groupType(groupBuyActivity.getGroupType())
            .takeLimitCount(groupBuyActivity.getTakeLimitCount())
            .target(groupBuyActivity.getTarget())
            .validTime(groupBuyActivity.getValidTime())
            .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
            .startTime(groupBuyActivity.getStartTime())
            .endTime(groupBuyActivity.getEndTime())
            .tagId(groupBuyActivity.getTagId())
            .tagScope(groupBuyActivity.getTagScope())
            .build();
    }

    /**
     * 统计指定用户在某个拼团活动下的已参与订单数
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 用户已参与次数
     */
    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);
    }

}
