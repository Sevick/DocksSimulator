package com.fbytes.docksimulator;

import javafx.collections.ObservableListBase;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by S on 01.09.2016.
 */
public class ObservableConcurrentQueue<E> extends ObservableListBase<E> implements Queue<E> {


    private ConcurrentLinkedQueue<E> statsCollection=new ConcurrentLinkedQueue<E>();


    @Override
    public E get(int index) {
        Iterator<E> iterator = statsCollection.iterator();
        for (int i = 0; i < index; i++) iterator.next();
        return iterator.next();
    }

    @Override
    public int size() {
        return statsCollection.size();
    }

    @Override
    public boolean offer(E e) {
        beginChange();
        boolean result=statsCollection.offer(e);
        endChange();
        return result;
    }

    @Override
    public E remove() {
        beginChange();
        E result=statsCollection.remove();
        endChange();
        return result;
    }

    @Override
    public E poll() {
        beginChange();
        E result=statsCollection.poll();
        endChange();
        return result;
    }

    @Override
    public E element() {
        beginChange();
        E result=statsCollection.element();
        endChange();
        return result;
    }

    @Override
    public E peek(){
        beginChange();
        E result=statsCollection.peek();
        endChange();
        return result;
    }
}
