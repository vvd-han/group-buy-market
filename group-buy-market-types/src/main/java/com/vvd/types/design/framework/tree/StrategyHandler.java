package com.vvd.types.design.framework.tree;

/**
 * @author hanjiazhi
 * @description 策略处理器
 * @create 2026/11/3 15:15
 */
public interface StrategyHandler<T, D, R> {

    StrategyHandler DEFAULT = (T, D) -> null;

    R apply(T requestParameter, D dynamicContext) throws Exception;

}
