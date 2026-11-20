package com.vvd.domain.activity.model.valobj;


import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum DiscountTypeEnum {

    BASE(0, "基础优惠"),
    TAG(1, "人群标签"),
    ;

    private Integer code;
    private String info;

    public static DiscountTypeEnum get(Integer code) {
        if (BASE.code.equals(code)) {
            return BASE;
        }
        if (TAG.code.equals(code)) {
            return TAG;
        }
        throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
    }

}
