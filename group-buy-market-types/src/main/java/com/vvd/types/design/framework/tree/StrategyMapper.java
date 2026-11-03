package com.vvd.types.design.framework.tree;

/**
 * @author hanjiazhi
 * @description 策略映射器
 * @create 2026/11/3 15:51
 */
public interface StrategyMapper<T, D, R> {

    /**
     * 获取待执行策略
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 返参
     * @throws Exception 异常
     */
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;

}