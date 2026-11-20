package com.vvd.infrastructure.adapter.repository;

import com.vvd.domain.tag.adapter.repository.ITagRepository;
import com.vvd.domain.tag.model.entity.CrowdTagsJobEntity;
import com.vvd.infrastructure.dao.ICrowdTagsDao;
import com.vvd.infrastructure.dao.ICrowdTagsDetailDao;
import com.vvd.infrastructure.dao.ICrowdTagsJobDao;
import com.vvd.infrastructure.dao.po.CrowdTagsJob;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepository implements ITagRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao;

    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;

    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Override
    public boolean lockTag(String tagId) {
        return crowdTagsDao.lockTag(tagId) != null;
    }

    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob request = CrowdTagsJob.builder().tagId(tagId).batchId(batchId).build();
        CrowdTagsJob job = crowdTagsJobDao.queryCrowdTagsJob(request);
        if (job == null) {
            return null;
        }
        return CrowdTagsJobEntity.builder()
            .tagType(job.getTagType())
            .tagRule(job.getTagRule())
            .statStartTime(job.getStatStartTime())
            .statEndTime(job.getStatEndTime())
            .status(job.getStatus())
            .build();
    }

    @Override
    public void addCrowdTagsUsers(String tagId, List<String> userIds) {
        crowdTagsDetailDao.addCrowdTagsUsers(tagId, userIds);
    }

    @Override
    public void updateCrowdTagsStatistics(String tagId) {
        crowdTagsDao.updateCrowdTagsStatistics(tagId);
    }

    @Override
    public boolean completeCrowdTagsJob(String tagId, String batchId) {
        return crowdTagsJobDao.completeCrowdTagsJob(tagId, batchId) == 1;
    }
}
