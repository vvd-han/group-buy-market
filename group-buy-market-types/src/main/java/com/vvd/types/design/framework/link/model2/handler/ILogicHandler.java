package com.vvd.types.design.framework.link.model2.handler;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/27 11:47
 */
public interface ILogicHandler<T, D, R> {

    default R next(T requestParameter, D dynamicContext) {
        return null;
    }

    R apply(T requestParameter, D dynamicContext) throws Exception;

}
