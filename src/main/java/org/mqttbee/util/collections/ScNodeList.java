package org.mqttbee.util.collections;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class ScNodeList<E> implements ScListNode, Iterable<E> {

    @Nullable
    private NextNode<E> head;
    @NotNull
    private final ScLinkedListIterator iterator = new ScLinkedListIterator();

    public Handle<E> add(@NotNull final E element) {
        return head = new NextNode<>(element, this, head);
    }

    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean removeNext() {
        if (head == null) {
            return true;
        }
        final NextNode<E> next = head.next;
        head = next;
        if (next == null) {
            return true;
        }
        next.prev = this;
        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }


    private static class NextNode<E> implements ScListNode, Handle<E> {

        private final E element;
        @NotNull
        private ScListNode prev;
        @Nullable
        private NextNode<E> next;

        NextNode(@NotNull final E element, @NotNull final ScListNode prev, @Nullable final NextNode<E> next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
        }

        @Override
        public boolean removeNext() {
            if (next != null) {
                final NextNode<E> next = this.next.next;
                this.next = next;
                if (next != null) {
                    next.prev = this;
                }
            }
            return false;
        }

        @Override
        public E getElement() {
            return element;
        }

        @Override
        public boolean remove() {
            return prev.removeNext();
        }

    }


    public interface Handle<E> {

        E getElement();

        boolean remove();

    }


    private class ScLinkedListIterator implements Iterator<E> {

        private NextNode<E> current;
        private NextNode<E> next;

        private void clear() {
            current = null;
            next = head;
        }

        @Override
        public boolean hasNext() {
            current = next;
            if (current == null) {
                return false;
            }
            next = current.next;
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
            current.remove();
        }

    }

}
