package org.ps;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<E> {

    private record Node<E>(E value, Node<E> next) {

    }

    private final AtomicReference<Node<E>> top = new AtomicReference<>();

    public void push(E item) {
        Node<E> newNode;
        Node<E> oldTop;
        do {
            oldTop = top.get();
            newNode = new Node<>(item, oldTop);
        } while (!top.compareAndSet(oldTop, newNode));
    }

    public E pop() {
        Node<E> oldTop;
        Node<E> newTop;
        do {
            oldTop = top.get();
            if (oldTop == null) return null;
            newTop = oldTop.next();
        } while (!top.compareAndSet(oldTop, newTop));
        return oldTop.value();
    }
}