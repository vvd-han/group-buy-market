package com.vvd.domain.activity.service.trial.thread;

import com.vvd.domain.activity.adapter.repository.IActivityRepository;
import com.vvd.domain.activity.model.valobj.SkuVO;
import java.util.concurrent.Callable;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/11 16:08
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {

    private final String source;
    private final String channel;
    private final String goodsId;

    private final IActivityRepository activityRepository;

    public QuerySkuVOFromDBThreadTask(String source, String channel, String goodsId,
        IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuBySCGoodsId(source, channel, goodsId);
    }

}
