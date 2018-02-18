package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class Ranges {

    private Range rootRange;

    public Ranges(final int maxId) {
        rootRange = new Range(0, maxId + 1);
    }

    public int getId() {
        return rootRange.getId();
    }

    public void returnId(final int id) {
        rootRange = rootRange.returnId(id);
    }


    private static class Range {

        private int start;
        private int end;
        private Range next;

        private Range(final int start, final int end) {
            this.start = start;
            this.end = end;
        }

        private Range(final int start, final int end, @NotNull final Range next) {
            this.start = start;
            this.end = end;
            this.next = next;
        }

        private int getId() {
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

        @NotNull
        private Range returnId(final int id) {
            Range range = this;
            if (id == start - 1) {
                start--;
            } else if (id == end) {
                end++;
                if ((next != null) && (end == next.start)) {
                    end = next.end;
                    next = next.next;
                }
            } else if (id < start - 1) {
                range = new Range(id, id + 1, this);
            } else if (next != null) {
                next = next.returnId(id);
                if (end == next.start) {
                    end = next.end;
                    next = next.next;
                }
            } else {
                next = new Range(id, id + 1);
            }
            return range;
        }

    }

}
