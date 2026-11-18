package com.vvd.domain.tag.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.fenum.qual.SwingElementOrientation;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/18 22:21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrowdTagsJobEntity {

    /**
     * 标签类型（参与量、消费金额）
     */
    private Integer tagType;
    /**
     * 标签规则（限定类型 N次）
     */
    private String tagRule;
    /**
     * 统计数据，开始时间
     */
    private Date statStartTime;
    /**
     * 统计数据，结束时间
     */
    private Date statEndTime;

}

