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

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class NodeList<N extends NodeList.Node<N>> {

    public static abstract class Node<N extends Node> {

        @Nullable N prev, next;

        public @Nullable N getPrev() {
            return prev;
        }

        public @Nullable N getNext() {
            return next;
        }
    }

    private @Nullable N first, last;
    private int size;

    public void add(final @NotNull N node) {
        assert node.prev == null;
        assert node.next == null;

        final N last = this.last;
        if (last == null) {
            first = this.last = node;
        } else {
            last.next = node;
            node.prev = last;
            this.last = node;
        }
        size++;
    }

    public void remove(final @NotNull N node) {
        assert (node.prev != null) || (node == first);
        assert (node.next != null) || (node == last);
        assert size > 0;

        final N prev = node.prev;
        final N next = node.next;
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
        }
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
        size--;
    }

    public void replace(final @NotNull N oldNode, final @NotNull N newNode) {
        assert (oldNode.prev != null) || (oldNode == first);
        assert (oldNode.next != null) || (oldNode == last);
        assert newNode.prev == null;
        assert newNode.next == null;
        assert size > 0;

        final N prev = oldNode.prev;
        final N next = oldNode.next;
        newNode.prev = prev;
        newNode.next = next;
        if (prev == null) {
            first = newNode;
        } else {
            prev.next = newNode;
        }
        if (next == null) {
            last = newNode;
        } else {
            next.prev = newNode;
        }
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    public @Nullable N getFirst() {
        return first;
    }

    public @Nullable N getLast() {
        return last;
    }
}
