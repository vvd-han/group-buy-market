package com.vvd.types.design.framework.link.model1;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/27 11:36
 */
public interface ILogicChainArmory<T, D, R> {

    ILogicLink<T, D, R> next();

    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);

}
