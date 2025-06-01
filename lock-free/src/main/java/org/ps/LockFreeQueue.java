package org.ps;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class LockFreeQueue<E> {

    private record Node<E>(E value, AtomicReference<Node<E>> next) {

    }

    private final AtomicReference<Node<E>> head;
    private final AtomicReference<Node<E>> tail;
    private final AtomicInteger size;

    public LockFreeQueue() {
        head = new AtomicReference<>();
        tail = new AtomicReference<>();
        size = new AtomicInteger();
    }

    public void enqueue(E item) {
        Node<E> newNode = new Node<>(item, null);

        while (true) {
            Node<E> curTail = tail.get();
            Node<E> tailNext = curTail.next.get();

            if (curTail == tail.get()) {
                if (tailNext == null) {
                    if (curTail.next.compareAndSet(null, newNode)) {
                        tail.compareAndSet(curTail, newNode);
                        size.incrementAndGet();
                        return;
                    }
                } else {
                    tail.compareAndSet(curTail, tailNext);
                }
            }
        }
    }

    public E dequeue() {
        while (true) {
            Node<E> curHead = head.get();
            Node<E> curTail = tail.get();
            Node<E> headNext = curHead.next.get();

            if (curHead == head.get()) {
                if (headNext == null) {
                    return null;
                }
                if (curHead == curTail) {
                    tail.compareAndSet(curTail, headNext);
                } else {
                    E value = headNext.value;
                    if (head.compareAndSet(curHead, headNext)) {
                        size.decrementAndGet();
                        return value;
                    }
                }
            }
        }
    }
}