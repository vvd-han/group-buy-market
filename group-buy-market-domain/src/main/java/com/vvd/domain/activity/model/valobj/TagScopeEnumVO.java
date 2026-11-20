package com.vvd.domain.activity.model.valobj;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/20 15:42
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TagScopeEnumVO {

    VISIBLE(true, false, "是否可看见拼团"),
    ENABLE(true, false, "是否可参与拼团"),
    ;

    private Boolean allow;
    private Boolean refuse;
    private String desc;

}
