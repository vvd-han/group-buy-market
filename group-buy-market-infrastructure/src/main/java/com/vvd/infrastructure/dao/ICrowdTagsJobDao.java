package com.vvd.infrastructure.dao;

import com.vvd.infrastructure.dao.po.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hanjiazhi
 * @description 人群标签任务
 * @create 2024-12-28 11:50
 */
@Mapper
public interface ICrowdTagsJobDao {

    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);

    int completeCrowdTagsJob(@Param("tagId") String tagId, @Param("batchId") String batchId);

}
