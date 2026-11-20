package com.vvd.infrastructure.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hanjiazhi
 * @description 人群标签明细
 * @create 2024-12-28 11:49
 */
@Mapper
public interface ICrowdTagsDetailDao {

    void addCrowdTagsUsers(@Param("tagId") String tagId, @Param("userIds") List<String> userIds);

    boolean containsUser(@Param("tagId") String tagId, @Param("userId") String userId);

}
