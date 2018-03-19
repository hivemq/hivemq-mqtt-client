package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
public class ScLinkedList<E> implements Iterable<E> {

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static class Builder<E> {

        private Node<E> head;

        public Builder<E> add(@NotNull final E e) {
            final Node<E> node = new Node<>(e);
            node.next = head;
            head = node;
            return this;
        }

        public ScLinkedList<E> build() {
            return new ScLinkedList<>(head);
        }

    }


    private Node<E> head;
    private final ScLinkedQueueIterator iterator = new ScLinkedQueueIterator();

    private ScLinkedList(@Nullable final Node<E> head) {
        this.head = head;
    }

    public boolean isEmpty() {
        return head == null;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }


    private static class Node<E> {

        private final E element;
        private Node<E> next;

        private Node(@NotNull final E element) {
            this.element = element;
        }

    }


    private class ScLinkedQueueIterator implements Iterator<E> {

        private Node<E> previous;
        private Node<E> current;
        private Node<E> next;

        private void clear() {
            previous = null;
            current = null;
            next = head;
        }

        @Override
        public boolean hasNext() {
            previous = current;
            current = next;
            if (current == null) {
                return false;
            }
            next = next.next;
            return true;
        }

        @Override
        public E next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.element;
        }

        @Override
        public void remove() {
            if (previous == null) {
                head = next;
            } else {
                previous.next = next;
            }
            current = previous;
        }

    }

}
