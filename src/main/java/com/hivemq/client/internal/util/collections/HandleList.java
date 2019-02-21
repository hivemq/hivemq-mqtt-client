/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.util.collections;

import com.hivemq.client.internal.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class HandleList<E> extends HandleListNode<E> implements Iterable<E> {

    private final @NotNull HandleListIterator iterator = new HandleListIterator();

    public @NotNull Handle<E> add(final @NotNull E element) {
        return next = new Handle<>(element, this, next);
    }

    public boolean isEmpty() {
        return next == null;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        iterator.clear();
        return iterator;
    }

    public static class Handle<E> extends HandleListNode<E> {

        final @NotNull E element;
        @NotNull HandleListNode prev;

        Handle(final @NotNull E element, final @NotNull HandleListNode prev, final @Nullable Handle<E> next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
        }

        public @NotNull E getElement() {
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

    private class HandleListIterator implements Iterator<E> {

        private @Nullable Handle<E> current;
        private @Nullable Handle<E> next;

        private void clear() {
            current = null;
            next = HandleList.this.next;
        }

        @Override
        public boolean hasNext() {
            return (next != null);
        }

        @Override
        public @NotNull E next() {
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
            if (current == null) {
                throw new IllegalStateException();
            }
            current.remove();
            current = null;
        }
    }
}
