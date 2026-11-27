package com.vvd.infrastructure.dao;

import com.vvd.infrastructure.dao.po.GroupBuyActivity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 拼团活动Dao
 * @create 2024-12-07 10:10
 */
@Mapper
public interface IGroupBuyActivityDao {

    List<GroupBuyActivity> queryGroupBuyActivityList();

    GroupBuyActivity queryValidGroupBuyActivityId(Long activityId);

    /**
     * 根据活动ID查询拼团活动配置（不过滤状态，由领域层规则链做状态与有效期校验）
     *
     * @param activityId 活动ID
     * @return 拼团活动持久化对象
     */
    GroupBuyActivity queryGroupBuyActivityByActivityId(Long activityId);

}
