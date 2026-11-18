package com.vvd.infrastructure.adapter.repository;

import com.vvd.domain.tag.adapter.repository.ITagRepository;
import com.vvd.domain.tag.model.entity.CrowdTagsJobEntity;
import com.vvd.infrastructure.dao.ICrowdTagsDao;
import com.vvd.infrastructure.dao.ICrowdTagsDetailDao;
import com.vvd.infrastructure.dao.ICrowdTagsJobDao;
import com.vvd.infrastructure.dao.po.CrowdTags;
import com.vvd.infrastructure.dao.po.CrowdTagsDetail;
import com.vvd.infrastructure.dao.po.CrowdTagsJob;
import com.vvd.infrastructure.redis.IRedisService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/18 22:31
 */
@Repository
public class TagRepository implements ITagRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao;

    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;

    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Resource
    private IRedisService redisService;


    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob crowdTagsJobReq = new CrowdTagsJob();
        crowdTagsJobReq.setTagId(tagId);
        crowdTagsJobReq.setBatchId(batchId);

        CrowdTagsJob crowdTagsJobRes = crowdTagsJobDao.queryCrowdTagsJob(crowdTagsJobReq);

        if (null == crowdTagsJobRes) {
            return null;
        }

        return CrowdTagsJobEntity.builder()
            .tagType(crowdTagsJobRes.getTagType())
            .tagRule(crowdTagsJobRes.getTagRule())
            .statStartTime(crowdTagsJobRes.getStatStartTime())
            .statEndTime(crowdTagsJobRes.getStatEndTime())
            .build();
    }

    @Override
    public void addCrowdTagsUserId(String tagId, String userId) {
        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail();
        crowdTagsDetailReq.setTagId(tagId);
        crowdTagsDetailReq.setUserId(userId);

        try {
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);

            // 获取BitSet
            RBitSet bitSet = redisService.getBitSet(tagId);
            bitSet.set(redisService.getIndexFromUserId(userId), true);
        } catch (DuplicateKeyException ignore) {
            // 忽略唯一索引冲突
        }

    }

    @Override
    public void updateCrowdTagsStatistics(String tagId, int size) {
        CrowdTags crowdTagsReq = new CrowdTags();
        crowdTagsReq.setTagId(tagId);
        crowdTagsReq.setStatistics(size);

        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsReq);
    }

}
