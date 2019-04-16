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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
public class MqttTopicAliasAutoMapping implements MqttTopicAliasMapping {

    private static final byte OVERSIZE = 8; // TODO configurable
    private static final byte RETAIN = 10; // TODO configurable
    private static final byte OVERWRITE_TRIES = 3; // TODO configurable

    private final int topicAliasMaximum;
    private final @NotNull Map<String, Entry> map = new HashMap<>();
    private @Nullable Entry lowest; // entry with lowest priority
    private long accessCounter; // strictly incremented
    private byte overwriteTries;// attempts to overwrite the entry with lowest priority out of these with a topic alias
    private byte fullOverwriteTries; // attempts to overwrite the entry with lowest priority out of all entries

    public MqttTopicAliasAutoMapping(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public int onPublish(final @NotNull MqttTopicImpl topic) {
        accessCounter++;
        final String topicString = topic.toString();
        final Entry entry = map.get(topicString);
        if (entry != null) { // entry already present
            final long priority = entry.access(accessCounter);
            if ((entry.topicAlias != DEFAULT_NO_TOPIC_ALIAS) && (entry.lower != null) &&
                    (entry.lower.topicAlias == DEFAULT_NO_TOPIC_ALIAS)) {
                if (overwriteTries > 0) {
                    overwriteTries--; // reduce chance to overwrite entry with lowest priority & topic alias if accessed
                }
            } else if ((entry == lowest) && (fullOverwriteTries > 0)) {
                fullOverwriteTries--; // reduce chance to overwrite entry with lowest priority if accessed
            }
            swapNewer(entry, priority);
            return entry.topicAlias; // topic alias is 0 if entry is part of oversize
        }
        if (map.size() < topicAliasMaximum + OVERSIZE) {
            final Entry newEntry = new Entry(accessCounter);
            if (map.size() < topicAliasMaximum) { // still unused topic aliases available
                newEntry.setNewTopicAlias(map.size() + 1);
            }
            map.put(topicString, newEntry);
            if (lowest != null) {
                newEntry.higher = lowest;
                lowest.lower = newEntry;
            }
            lowest = newEntry;
            swapNewer(newEntry, newEntry.priority());
            return newEntry.topicAlias;
        }
        fullOverwriteTries++;
        if (fullOverwriteTries < OVERWRITE_TRIES) {
            return DEFAULT_NO_TOPIC_ALIAS; // do not overwrite entry with lowest priority immediately
        }
        fullOverwriteTries = 0;
        map.values().remove(lowest);
        final Entry newEntry = new Entry(accessCounter);
        map.put(topicString, newEntry);
        if (lowest != null) {
            newEntry.topicAlias = lowest.topicAlias;
            final Entry higher = lowest.higher;
            newEntry.higher = higher;
            if (higher != null) {
                higher.lower = newEntry;
            }
        }
        lowest = newEntry;
        return newEntry.topicAlias;
    }

    private void swapNewer(final @NotNull Entry entry, final long priority) {
        while (true) {
            final Entry higher = entry.higher;
            if (higher == null) {
                break;
            }
            final long newerPriority = higher.priority(accessCounter);
            if (newerPriority >= priority) {
                break;
            }
            if ((entry.topicAlias == DEFAULT_NO_TOPIC_ALIAS) && (higher.topicAlias != DEFAULT_NO_TOPIC_ALIAS)) {
                overwriteTries++;
                if (overwriteTries < OVERWRITE_TRIES) {
                    break; // do not swap immediately if entry would overwrite the topic alias of the next entry
                }
                overwriteTries = 0;
                entry.setNewTopicAlias(higher.topicAlias);
                higher.topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            }
            higher.lower = entry.lower;
            entry.higher = higher.higher;
            entry.lower = higher;
            higher.higher = entry;
            if (entry == lowest) {
                lowest = higher;
            }
        }
    }

    @Override
    public @NotNull String toString() {
        final TreeSet<Map.Entry<String, Entry>> sorted = new TreeSet<>((o1, o2) -> {
            Entry entry = o1.getValue().higher;
            while (entry != null) {
                if (entry == o2.getValue()) {
                    return 1;
                }
                entry = entry.higher;
            }
            return -1;
        });
        sorted.addAll(map.entrySet());
        final StringBuilder s = new StringBuilder();
        for (final Map.Entry<String, Entry> entry : sorted) {
            s.append(" -> ").append(entry.toString());
        }
        return s.toString();
    }

    static class Entry {

        int topicAlias;
        long used; // number of accesses, decays over time
        long access; // stamp when the entry was last accessed
        @Nullable Entry higher; // entry with the next higher priority
        @Nullable Entry lower; // entry with the next lower priority

        Entry(final long accessCounter) {
            topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            used = 1;
            access = accessCounter;
        }

        void setNewTopicAlias(final int topicAlias) {
            this.topicAlias = topicAlias | TOPIC_ALIAS_FLAG_NEW;
        }

        long access(final long accessCounter) {
            update(accessCounter);
            topicAlias &= TOPIC_ALIAS_FLAG; // clear NEW_TOPIC_ALIAS bit
            used++;
            access = accessCounter;
            return priority();
        }

        long priority(final long accessCounter) {
            update(accessCounter);
            return priority();
        }

        long priority() {
            return used + access;
        }

        private void update(final long accessCounter) {
            final long decay = Math.max(accessCounter - access - RETAIN, 0);
            used = Math.max(used - decay, 1);
        }

        @Override
        public @NotNull String toString() {
            final int topicAlias = this.topicAlias & TOPIC_ALIAS_FLAG;
            return ((topicAlias == DEFAULT_NO_TOPIC_ALIAS) ? "-" : topicAlias) + " (used: " + used + ", access: " +
                    access + ")";
        }
    }
}
