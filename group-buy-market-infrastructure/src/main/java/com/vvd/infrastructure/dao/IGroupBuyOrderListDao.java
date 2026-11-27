package com.vvd.infrastructure.dao;

import com.vvd.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/25 20:58
 */
@Mapper
public interface IGroupBuyOrderListDao {

    void insert(GroupBuyOrderList groupBuyOrderListReq);

    GroupBuyOrderList queryGroupBuyOrderRecordByOutTradeNo(GroupBuyOrderList groupBuyOrderListReq);

    /**
     * 统计指定用户在某拼团活动下的历史参与订单数（命中联合索引 idx_user_id_activity_id）
     *
     * @param groupBuyOrderListReq 查询条件（需填充 userId、activityId）
     * @return 用户已参与次数
     */
    Integer queryOrderCountByActivityId(GroupBuyOrderList groupBuyOrderListReq);

}
