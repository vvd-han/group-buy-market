package com.vvd.domain.tag.service.impl;

import com.vvd.domain.tag.adapter.repository.ITagRepository;
import com.vvd.domain.tag.model.entity.CrowdTagsJobEntity;
import com.vvd.domain.tag.service.ITagService;
import com.vvd.types.common.Constants;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagServiceImpl implements ITagService {

    private static final Integer MANUAL_MEMBERSHIP = 2;
    private static final Integer READY = 1;
    private static final Integer COMPLETED = 3;

    @Resource
    private ITagRepository repository;

    @Override
    @Transactional
    public void execTagBatchJob(String tagId, String batchId) {
        if (StringUtils.isAnyBlank(tagId, batchId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        if (!repository.lockTag(tagId)) {
            throw invalidJob();
        }
        CrowdTagsJobEntity job = repository.queryCrowdTagsJobEntity(tagId, batchId);
        if (job == null) {
            throw invalidJob();
        }
        if (COMPLETED.equals(job.getStatus())) {
            return;
        }
        if (!READY.equals(job.getStatus()) || !MANUAL_MEMBERSHIP.equals(job.getTagType())
            || StringUtils.isBlank(job.getTagRule())) {
            throw invalidJob();
        }
        List<String> members = new ArrayList<>();
        for (String member : job.getTagRule().split(Constants.SPLIT, -1)) {
            String userId = member.trim();
            if (userId.isEmpty() || userId.codePointCount(0, userId.length()) > 128) {
                throw invalidJob();
            }
            members.add(userId);
        }
        repository.addCrowdTagsUsers(tagId, members);
        repository.updateCrowdTagsStatistics(tagId);
        if (!repository.completeCrowdTagsJob(tagId, batchId)) {
            throw invalidJob();
        }
    }

    private AppException invalidJob() {
        return new AppException(ResponseCode.E0008.getCode(), ResponseCode.E0008.getInfo());
    }
}
