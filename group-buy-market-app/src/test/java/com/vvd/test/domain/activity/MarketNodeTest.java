package com.vvd.test.domain.activity;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.service.trial.factory.DefaultActivityStrategyFactory.DynamicContext;
import com.vvd.domain.activity.service.trial.node.MarketNode;
import com.vvd.types.exception.AppException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MarketNodeTest {

    @Test(timeout = 4000)
    public void timeoutCancelsBothQueries() throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        CountDownLatch cancelled = new CountDownLatch(2);
        CountDownLatch started = new CountDownLatch(2);
        IActivityRepository repository = mock(IActivityRepository.class);
        when(repository.querySCSkuActivityBySCGoodsId("s", "c", "g"))
            .thenAnswer(invocation -> blockUntilCancelled(started, cancelled));
        when(repository.querySkuBySCGoodsId("s", "c", "g"))
            .thenAnswer(invocation -> blockUntilCancelled(started, cancelled));
        try {
            assertBusy(buildNode(executor, repository));
            assertTrue(cancelled.await(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(timeout = 4000)
    public void rejectionCancelsFirstQueryAndSkipsSecondQuery() throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        IActivityRepository repository = mock(IActivityRepository.class);
        when(repository.querySCSkuActivityBySCGoodsId("s", "c", "g")).thenAnswer(invocation -> {
            new CountDownLatch(1).await();
            return null;
        });
        try {
            assertBusy(buildNode(executor, repository));
            assertTrue(executor.getQueue().isEmpty());
            executor.shutdown();
            assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
            verify(repository, never()).querySkuBySCGoodsId("s", "c", "g");
        } finally {
            executor.shutdownNow();
        }
    }

    private Object blockUntilCancelled(CountDownLatch started, CountDownLatch cancelled) throws Exception {
        started.countDown();
        try {
            started.await();
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            cancelled.countDown();
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private MarketNode buildNode(ThreadPoolExecutor executor, IActivityRepository repository) {
        MarketNode node = new MarketNode();
        ReflectionTestUtils.setField(node, "threadPoolExecutor", executor);
        ReflectionTestUtils.setField(node, "repository", repository);
        return node;
    }

    private void assertBusy(MarketNode node) {
        MarketProductEntity request = MarketProductEntity.builder().source("s").channel("c").goodsId("g").build();
        assertEquals("E0006", assertThrows(AppException.class, () -> node.apply(request, new DynamicContext())).getCode());
    }
}
