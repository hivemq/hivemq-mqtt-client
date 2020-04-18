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
import com.hivemq.client.internal.util.Pow2Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class IntIndex<E> {

    private static final int MAX_CAPACITY = 1 << 30;

    public static class Spec<E> {

        private static final int DEFAULT_MIN_CAPACITY = 1 << 4;
        private static final float DEFAULT_NODE_THRESHOLD_FACTOR = 0.25f;

        final @NotNull ToIntFunction<? super E> keyFunction;
        final int minCapacity;
        final float nodeThresholdFactor;

        public Spec(final @NotNull ToIntFunction<? super E> keyFunction) {
            this(keyFunction, DEFAULT_MIN_CAPACITY, DEFAULT_NODE_THRESHOLD_FACTOR);
        }

        public Spec(final @NotNull ToIntFunction<? super E> keyFunction, final int minCapacity) {
            this(keyFunction, minCapacity, DEFAULT_NODE_THRESHOLD_FACTOR);
        }

        public Spec(final @NotNull ToIntFunction<? super E> keyFunction, final float nodeThresholdFactor) {
            this(keyFunction, DEFAULT_MIN_CAPACITY, nodeThresholdFactor);
        }

        public Spec(
                final @NotNull ToIntFunction<? super E> keyFunction, final int minCapacity,
                final float nodeThresholdFactor) {

            this.keyFunction = keyFunction;
            this.minCapacity = minCapacity;
            this.nodeThresholdFactor = nodeThresholdFactor;
        }
    }

    private final @NotNull Spec<E> spec;
    private @Nullable Object @NotNull [] table;
    private int size;
    private int nodeCount;
    private int nodeThreshold;

    public IntIndex(final @NotNull Spec<E> spec) {
        this.spec = spec;
        final int minCapacityPow2 = 1 << Pow2Util.roundToPowerOf2Bits(spec.minCapacity);
        table = new Object[minCapacityPow2];
        calcThresholds(minCapacityPow2);
    }

    public int size() {
        return size;
    }

    public @Nullable E put(final @NotNull E entry) {
        return put(entry, true);
    }

    public @Nullable E putIfAbsent(final @NotNull E entry) {
        return put(entry, false);
    }

    private @Nullable E put(final @NotNull E entry, final boolean overwrite) {
        final Object[] table = this.table;
        final int key = spec.keyFunction.applyAsInt(entry);
        final int index = key & (table.length - 1);
        final Object o = table[index];
        if (o == null) {
            table[index] = entry;
            added();
            return null;
        }
        if (o.getClass() == Node.class) {
            Node node = (Node) o;
            while (true) {
                if (node.key == key) {
                    final Object nodeValue = node.value;
                    if (overwrite) {
                        node.value = entry;
                    }
                    return cast(nodeValue);
                }
                final Object next = node.next;
                if (next.getClass() == Node.class) {
                    node = (Node) next;
                } else {
                    final E e = cast(next);
                    final int nextKey = spec.keyFunction.applyAsInt(e);
                    if (nextKey == key) {
                        if (overwrite) {
                            node.next = entry;
                        }
                        return e;
                    }
                    node.next = new Node(nextKey, next, entry);
                    added();
                    addedNode();
                    return null;
                }
            }
        }
        final E e = cast(o);
        final int oKey = spec.keyFunction.applyAsInt(e);
        if (oKey == key) {
            if (overwrite) {
                table[index] = entry;
            }
            return e;
        }
        table[index] = new Node(oKey, o, entry);
        added();
        addedNode();
        return null;
    }

    public @Nullable E get(final int key) {
        final Object[] table = this.table;
        final int index = key & (table.length - 1);
        final Object o = table[index];
        if (o == null) {
            return null;
        }
        if (o.getClass() == Node.class) {
            Node node = (Node) o;
            while (true) {
                if (node.key == key) {
                    return cast(node.value);
                }
                final Object next = node.next;
                if (next.getClass() == Node.class) {
                    node = (Node) next;
                } else {
                    final E e = cast(next);
                    if (spec.keyFunction.applyAsInt(e) == key) {
                        return e;
                    }
                    return null;
                }
            }
        }
        final E e = cast(o);
        if (spec.keyFunction.applyAsInt(e) == key) {
            return e;
        }
        return null;
    }

    public @Nullable E remove(final int key) {
        final Object[] table = this.table;
        final int index = key & (table.length - 1);
        final Object o = table[index];
        if (o == null) {
            return null;
        }
        if (o.getClass() == Node.class) {
            Node node = (Node) o;
            if (node.key == key) {
                table[index] = node.next;
                removedNode();
                removed();
                return cast(node.value);
            }
            Object next = node.next;
            if (next.getClass() != Node.class) {
                final E e = cast(next);
                if (spec.keyFunction.applyAsInt(e) == key) {
                    table[index] = node.value;
                    removedNode();
                    removed();
                    return e;
                }
                return null;
            }
            Node prevNode;
            while (true) {
                prevNode = node;
                node = (Node) next;
                if (node.key == key) {
                    prevNode.next = node.next;
                    removedNode();
                    removed();
                    return cast(node.value);
                }
                next = node.next;
                if (next.getClass() != Node.class) {
                    final E e = cast(next);
                    if (spec.keyFunction.applyAsInt(e) == key) {
                        prevNode.next = node.value;
                        removedNode();
                        removed();
                        return e;
                    }
                    return null;
                }
            }
        }
        final E e = cast(o);
        if (spec.keyFunction.applyAsInt(e) == key) {
            table[index] = null;
            removed();
            return e;
        }
        return null;
    }

    public void clear() {
        if (size > 0) {
            if (table.length == spec.minCapacity) {
                Arrays.fill(table, null);
            } else {
                table = new Object[spec.minCapacity];
            }
            size = 0;
            nodeCount = 0;
            calcThresholds(spec.minCapacity);
        }
    }

    public void forEach(final @NotNull Consumer<? super E> consumer) {
        for (final Object o : table) {
            if (o != null) {
                if (o.getClass() == Node.class) {
                    Node node = (Node) o;
                    while (true) {
                        consumer.accept(cast(node.value));
                        final Object next = node.next;
                        if (next.getClass() == Node.class) {
                            node = (Node) next;
                        } else {
                            consumer.accept(cast(next));
                            break;
                        }
                    }
                } else {
                    consumer.accept(cast(o));
                }
            }
        }
    }

    private void added() {
        size++;
    }

    private void addedNode() {
        if ((++nodeCount > nodeThreshold) && (table.length < MAX_CAPACITY)) {
            final Object[] oldTable = table;
            final int oldCapacity = oldTable.length;
            final int newCapacity = oldCapacity << 1;
            final int newMask = newCapacity - 1;
            final Object[] newTable = new Object[newCapacity];
            int newNodeCount = 0;

            for (int oldIndex = 0; oldIndex < oldCapacity; oldIndex++) {
                final Object o = oldTable[oldIndex];
                if (o != null) {
                    if (o.getClass() == Node.class) {
                        Node node = (Node) o;
                        Node low = null, prevLow = null;
                        Node high = null, prevHigh = null;
                        final int highIndex = oldIndex + oldCapacity;
                        while (true) {
                            if ((node.key & newMask) == oldIndex) {
                                if (low == null) {
                                    low = node;
                                    newTable[oldIndex] = node;
                                } else {
                                    prevLow = low;
                                    low = node;
                                    prevLow.next = low;
                                }
                            } else {
                                if (high == null) {
                                    high = node;
                                    newTable[highIndex] = node;
                                } else {
                                    prevHigh = high;
                                    high = node;
                                    prevHigh.next = high;
                                }
                            }
                            newNodeCount++;
                            final Object next = node.next;
                            if (next.getClass() == Node.class) {
                                node = (Node) next;
                            } else {
                                final E e = cast(next);
                                if ((spec.keyFunction.applyAsInt(e) & newMask) == oldIndex) {
                                    if (low == null) {
                                        newTable[oldIndex] = e;
                                    } else {
                                        low.next = e;
                                    }
                                    if (high != null) {
                                        if (prevHigh == null) {
                                            newTable[highIndex] = high.value;
                                        } else {
                                            prevHigh.next = high.value;
                                        }
                                        newNodeCount--;
                                    }
                                } else {
                                    if (high == null) {
                                        newTable[highIndex] = e;
                                    } else {
                                        high.next = e;
                                    }
                                    if (low != null) {
                                        if (prevLow == null) {
                                            newTable[oldIndex] = low.value;
                                        } else {
                                            prevLow.next = low.value;
                                        }
                                        newNodeCount--;
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        final int key = spec.keyFunction.applyAsInt(cast(o));
                        final int newIndex = key & newMask;
                        newTable[newIndex] = o;
                    }
                }
            }

            table = newTable;
            nodeCount = newNodeCount;
            calcThresholds(newCapacity);
        }
    }

    private void removedNode() {
        nodeCount--;
    }

    private void removed() {
        if ((--size < nodeThreshold) && (table.length > spec.minCapacity)) {
            final Object[] oldTable = table;
            final int oldCapacity = oldTable.length;
            final int newCapacity = oldCapacity >> 1;
            final Object[] newTable = new Object[newCapacity];
            int newNodeCount = nodeCount;

            System.arraycopy(oldTable, 0, newTable, 0, newCapacity);
            for (int oldIndex = newCapacity; oldIndex < oldCapacity; oldIndex++) {
                final Object old = oldTable[oldIndex];
                if (old != null) {
                    final int newIndex = oldIndex - newCapacity;
                    final Object o = newTable[newIndex];
                    if (o == null) {
                        newTable[newIndex] = old;
                    } else if (o.getClass() == Node.class) {
                        Node node = (Node) o;
                        Object next;
                        while (true) {
                            next = node.next;
                            if (next.getClass() == Node.class) {
                                node = (Node) next;
                            } else {
                                break;
                            }
                        }
                        node.next = new Node(spec.keyFunction.applyAsInt(cast(next)), next, old);
                        newNodeCount++;
                    } else {
                        newTable[newIndex] = new Node(spec.keyFunction.applyAsInt(cast(o)), o, old);
                        newNodeCount++;
                    }
                }
            }

            table = newTable;
            nodeCount = newNodeCount;
            calcThresholds(newCapacity);
        }
    }

    private void calcThresholds(final int capacity) {
        nodeThreshold = (int) (capacity * spec.nodeThresholdFactor);
    }

    private @NotNull E cast(final @NotNull Object o) {
        //noinspection unchecked
        return (E) o;
    }

    private static class Node {

        final int key;
        @NotNull Object value;
        @NotNull Object next;

        Node(final int key, final @NotNull Object value, final @NotNull Object next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
