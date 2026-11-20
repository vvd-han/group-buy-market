package com.vvd.domain.activity.model.valobj;

import com.vvd.types.common.Constants;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.EnumSet;
import org.apache.commons.lang3.StringUtils;

public enum TagScopeEnumVO {

    VISIBLE,
    ENABLE;

    public static EnumSet<TagScopeEnumVO> parse(String scope) {
        EnumSet<TagScopeEnumVO> scopes = EnumSet.noneOf(TagScopeEnumVO.class);
        if (StringUtils.isBlank(scope)) {
            return scopes;
        }
        for (String item : scope.split(Constants.SPLIT, -1)) {
            switch (item.trim()) {
                case "1":
                    scopes.add(VISIBLE);
                    break;
                case "2":
                    scopes.add(ENABLE);
                    break;
                default:
                    throw new AppException(ResponseCode.E0007.getCode(), ResponseCode.E0007.getInfo());
            }
        }
        return scopes;
    }
}
