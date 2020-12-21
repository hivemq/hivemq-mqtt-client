/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.internal.util;

import com.hivemq.client2.internal.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class Ranges {

    private @NotNull Range rootRange;

    public Ranges(final int minId, final int maxId) {
        rootRange = new Range(minId, maxId + 1);
    }

    public int getId() {
        if (rootRange.start == rootRange.end) {
            return -1;
        }
        final int id = rootRange.start;
        rootRange.start++;
        if ((rootRange.start == rootRange.end) && (rootRange.next != null)) {
            rootRange = rootRange.next;
        }
        return id;
    }

    public void returnId(final int id) {
        Range current = rootRange;
        if (id < current.start - 1) {
            rootRange = new Range(id, id + 1, current);
            return;
        }
        Range prev = current;
        current = returnId(current, id);
        while (current != null) {
            if (id < current.start - 1) {
                prev.next = new Range(id, id + 1, current);
                return;
            }
            prev = current;
            current = returnId(current, id);
        }
    }

    private @Nullable Range returnId(final @NotNull Range range, final int id) {
        final Range next = range.next;
        if (id == range.start - 1) {
            range.start = id;
            return null;
        }
        if (id < range.end) {
            throw new IllegalStateException("The id was already returned. This must not happen and is a bug.");
        }
        if (id == range.end) {
            if (next == null) {
                throw new IllegalStateException("The id is greater than maxId. This must not happen and is a bug.");
            }
            range.end++;
            if (range.end == next.start) {
                range.end = next.end;
                range.next = next.next;
            }
            return null;
        }
        if (next == null) {
            throw new IllegalStateException("The id is greater than maxId. This must not happen and is a bug.");
        }
        return next;
    }

    public int resize(final int maxId) {
        Range range = rootRange;
        while (range.end <= maxId) {
            final Range next = range.next;
            if (next == null) {
                range.end = maxId + 1;
                return 0;
            }
            range = next;
        }
        int count = range.start - (maxId + 1);
        if (count < 0) {
            count = 0;
        }
        while (range.next != null) {
            final Range next = range.next;
            count += next.start - range.end;
            range = next;
        }
        if (count == 0) {
            range.end = maxId + 1;
        }
        return count;
    }

    private static class Range {

        int start;
        int end;
        @Nullable Range next;

        Range(final int start, final int end) {
            this.start = start;
            this.end = end;
        }

        Range(final int start, final int end, final @NotNull Range next) {
            this.start = start;
            this.end = end;
            this.next = next;
        }
    }
}
