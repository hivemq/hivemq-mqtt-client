/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class HandleList<E> extends HandleListNode<E> implements Iterable<E> {

    @NotNull
    private final ScLinkedListIterator iterator = new ScLinkedListIterator();

    @NotNull
    public Handle<E> add(@NotNull final E element) {
        return next = new Handle<>(element, this, next);
    }

    public boolean isEmpty() {
        return next == null;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }

    public static class Handle<E> extends HandleListNode<E> {

        final E element;
        @NotNull
        HandleListNode prev;

        Handle(@NotNull final E element, @NotNull final HandleListNode prev, @Nullable final Handle<E> next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
        }

        @NotNull
        public E getElement() {
            return element;
        }

        public void remove() {
            final Handle<E> next = this.next;
            prev.next = next;
            if (next != null) {
                next.prev = prev;
            }
        }

    }

    private class ScLinkedListIterator implements Iterator<E> {

        private Handle<E> current;
        private Handle<E> next;

        private void clear() {
            current = null;
            next = HandleList.this.next;
        }

        @Override
        public boolean hasNext() {
            return (next != null);
        }

        @Override
        public E next() {
            final Handle<E> current = this.next;
            this.current = current;
            if (current == null) {
                throw new NoSuchElementException();
            }
            next = current.next;
            return current.element;
        }

        @Override
        public void remove() {
            current.remove();
        }

    }

}
