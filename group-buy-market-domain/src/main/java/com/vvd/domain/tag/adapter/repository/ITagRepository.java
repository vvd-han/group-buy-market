package com.vvd.domain.tag.adapter.repository;

import com.vvd.domain.tag.model.entity.CrowdTagsJobEntity;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/18 22:18
 */
public interface ITagRepository {

    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowdTagsUserId(String tagId, String userId);

    void updateCrowdTagsStatistics(String tagId, int size);

}
