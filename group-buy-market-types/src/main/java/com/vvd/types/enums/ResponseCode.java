package com.vvd.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),

    E0001("E0001", "不存在对应的折扣计算服务"),
    E0002("E0002", "无拼团营销配置"),
    E0003("E0003", "拼团活动不在有效期内"),
    E0004("E0004", "拼团优惠配置非法"),
    E0005("E0005", "拼团试算结果异常"),
    E0006("E0006", "拼团试算查询超时或服务繁忙"),
    E0007("E0007", "拼团活动配置非法"),
    E0008("E0008", "人群标签任务配置非法或任务状态不可执行"),
    ;

    private String code;
    private String info;

}