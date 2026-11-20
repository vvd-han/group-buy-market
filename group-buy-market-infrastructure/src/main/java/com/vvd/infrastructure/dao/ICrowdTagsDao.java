package com.vvd.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hanjiazhi
 * @description 人群标签
 * @create 2024-12-28 11:49
 */
@Mapper
public interface ICrowdTagsDao {

    String lockTag(@Param("tagId") String tagId);

    void updateCrowdTagsStatistics(@Param("tagId") String tagId);

}
