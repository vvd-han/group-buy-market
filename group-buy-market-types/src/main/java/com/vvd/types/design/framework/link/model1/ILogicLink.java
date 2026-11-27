package com.vvd.types.design.framework.link.model1;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/27 11:34
 */
public interface ILogicLink<T, D, R> extends ILogicChainArmory<T, D, R> {

    R apply(T requestParameter, D dynamicContext) throws Exception;

}

