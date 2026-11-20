package com.vvd.domain.tag.adapter.repository;

import com.vvd.domain.tag.model.entity.CrowdTagsJobEntity;
import java.util.List;

public interface ITagRepository {

    boolean lockTag(String tagId);

    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowdTagsUsers(String tagId, List<String> userIds);

    void updateCrowdTagsStatistics(String tagId);

    boolean completeCrowdTagsJob(String tagId, String batchId);

}
