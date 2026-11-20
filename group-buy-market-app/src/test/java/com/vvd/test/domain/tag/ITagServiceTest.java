package com.vvd.test.domain.tag;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.tag.service.ITagService;
import com.vvd.infrastructure.adapter.repository.TagRepository;
import com.vvd.types.exception.AppException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ITagServiceTest {

    @Resource
    private ITagService tagService;

    @Resource
    private IActivityRepository activityRepository;

    @Resource
    private JdbcTemplate jdbc;

    @MockBean
    private RedissonClient redis;

    @SpyBean
    private TagRepository tagRepository;

    @Test
    public void serializesBatchesAndCountsUniqueMembers() throws Exception {
        String tagId = UUID.randomUUID().toString();
        jdbc.update("insert into crowd_tags (tag_id, tag_name) values (?, 'batch test')", tagId);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            insertJob(tagId, "first", "Alpha,Beta,Alpha", 1);
            insertJob(tagId, "second", "Alpha,Gamma", 1);
            Future<?> first = executor.submit(() -> tagService.execTagBatchJob(tagId, "first"));
            Future<?> retry = executor.submit(() -> tagService.execTagBatchJob(tagId, "first"));
            Future<?> second = executor.submit(() -> tagService.execTagBatchJob(tagId, "second"));
            first.get(10, TimeUnit.SECONDS);
            retry.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);

            assertEquals(Integer.valueOf(3), jdbc.queryForObject(
                "select statistics from crowd_tags where tag_id = ?", Integer.class, tagId));
            assertEquals(Integer.valueOf(2), jdbc.queryForObject(
                "select count(*) from crowd_tags_job where tag_id = ? and status = 3", Integer.class, tagId));
            assertTrue(activityRepository.isTagCrowdRange(tagId, "Alpha"));
            assertFalse(activityRepository.isTagCrowdRange(tagId, "alpha"));
            assertFalse(activityRepository.isTagCrowdRange(tagId, "Alpha "));
        } finally {
            executor.shutdownNow();
            removeTag(tagId);
        }
    }

    @Test
    public void rejectsMissingAndInactiveJobsWithoutWritingMembers() {
        String tagId = UUID.randomUUID().toString();
        jdbc.update("insert into crowd_tags (tag_id, tag_name) values (?, 'job test')", tagId);
        try {
            insertJob(tagId, "initial", "Alpha", 0);
            insertJob(tagId, "malformed", "Alpha,", 1);
            for (String batchId : new String[]{"missing", "initial", "malformed"}) {
                assertEquals("E0008", assertThrows(AppException.class,
                    () -> tagService.execTagBatchJob(tagId, batchId)).getCode());
            }
            assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "select count(*) from crowd_tags_detail where tag_id = ?", Integer.class, tagId));
        } finally {
            removeTag(tagId);
        }
    }

    @Test
    public void rollsBackMembersAndStatisticsWhenCompletionFails() {
        String tagId = UUID.randomUUID().toString();
        jdbc.update("insert into crowd_tags (tag_id, tag_name) values (?, 'rollback test')", tagId);
        try {
            insertJob(tagId, "pending", "Alpha", 1);
            doThrow(new IllegalStateException("simulated completion failure"))
                .when(tagRepository).completeCrowdTagsJob(tagId, "pending");

            assertThrows(IllegalStateException.class, () -> tagService.execTagBatchJob(tagId, "pending"));
            assertFalse(activityRepository.isTagCrowdRange(tagId, "Alpha"));
            assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "select statistics from crowd_tags where tag_id = ?", Integer.class, tagId));
            assertEquals(Integer.valueOf(1), jdbc.queryForObject(
                "select status from crowd_tags_job where tag_id = ? and batch_id = 'pending'", Integer.class, tagId));
        } finally {
            removeTag(tagId);
        }
    }

    private void insertJob(String tagId, String batchId, String rule, int status) {
        jdbc.update("insert into crowd_tags_job (tag_id, batch_id, tag_type, tag_rule, status) values (?, ?, 2, ?, ?)",
            tagId, batchId, rule, status);
    }

    private void removeTag(String tagId) {
        jdbc.update("delete from crowd_tags_detail where tag_id = ?", tagId);
        jdbc.update("delete from crowd_tags_job where tag_id = ?", tagId);
        jdbc.update("delete from crowd_tags where tag_id = ?", tagId);
    }
}
