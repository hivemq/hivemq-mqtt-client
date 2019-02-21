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

package com.hivemq.client.internal.util;

import com.hivemq.client.internal.annotations.NotThreadSafe;
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
        return rootRange.getId();
    }

    public void returnId(final int id) {
        rootRange = rootRange.returnId(id);
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

        int getId() {
            if (start == end) {
                return -1;
            }
            final int id = this.start;
            start++;
            if ((start == end) && (next != null)) {
                start = next.start;
                end = next.end;
                next = next.next;
            }
            return id;
        }

        @NotNull Range returnId(final int id) {
            Range range = this;
            if (id < start - 1) {
                range = new Range(id, id + 1, this);
            } else if (id == start - 1) {
                start--;
            } else if (id < end) {
                throw new IllegalStateException("The id was already returned. This must not happen and is a bug.");
            } else if (id == end) {
                if (next == null) {
                    throw new IllegalStateException("The id is greater than maxId. This must not happen and is a bug.");
                }
                end++;
                if (end == next.start) {
                    end = next.end;
                    next = next.next;
                }
            } else if (next != null) {
                next = next.returnId(id);
            } else {
                throw new IllegalStateException("The id is greater than maxId. This must not happen and is a bug.");
            }
            return range;
        }
    }
}
