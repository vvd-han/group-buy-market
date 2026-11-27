package com.vvd.types.design.framework.link.model2.chain;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/27 11:46
 */
public interface ILink<E> {

    boolean add(E e);

    boolean addFirst(E e);

    boolean addLast(E e);

    boolean remove(Object o);

    E get(int index);

    void printLinkList();

}
