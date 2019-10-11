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
import com.hivemq.client.internal.util.collections.Index;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
public class MqttTopicAliasAutoMapping implements MqttTopicAliasMapping {

    private static final byte OVERSIZE = 4;
    private static final byte RETAIN = 8;
    private static final byte OVERWRITE_COST_MIN = 2;
    private static final byte OVERWRITE_COST_MAX = 126;
    private static final byte OVERWRITE_COST_INC = 2;
    private static final @NotNull Index.Spec<Entry, String> INDEX_SPEC = new Index.Spec<>(entry -> entry.topic);

    private final int topicAliasMaximum;
    private final @NotNull Index<Entry, String> map = new Index<>(INDEX_SPEC);
    private @Nullable Entry lowest; // entry with lowest priority
    private long accessCounter; // strictly incremented
    private byte overwriteTries;
    private byte overwriteCost = OVERWRITE_COST_MIN;
    private byte fullOverwriteTries;
    private byte fullOverwriteCost = OVERWRITE_COST_MIN;

    public MqttTopicAliasAutoMapping(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public int onPublish(final @NotNull MqttTopicImpl topic) {
        final long accessCounter = ++this.accessCounter;
        final String topicString = topic.toString();
        final Entry entry = map.get(topicString);
        if (entry != null) { // entry already present
            entry.access(accessCounter);
            if (entry.topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                if (overwriteCost > OVERWRITE_COST_MIN) {
                    overwriteCost--;
                }
                if (fullOverwriteCost > OVERWRITE_COST_MIN) {
                    fullOverwriteCost--;
                }
                if (entry.lower != null) {
                    if (entry.lower.topicAlias == DEFAULT_NO_TOPIC_ALIAS) {
                        overwriteTries = 0;
                    }
                } else {
                    fullOverwriteTries = 0;
                }
            }
            swapNewer(entry, accessCounter);
            return entry.topicAlias; // topic alias is 0 if entry is part of oversize
        }
        final Entry newEntry = new Entry(topicString, accessCounter);
        if (map.size() < topicAliasMaximum + OVERSIZE) {
            if (map.size() < topicAliasMaximum) { // still unused topic aliases available
                newEntry.setNewTopicAlias(map.size() + 1);
            }
            map.put(newEntry);
            if (lowest != null) {
                newEntry.higher = lowest;
                lowest.lower = newEntry;
            }
        } else {
            final Entry lowest = this.lowest;
            assert lowest != null;
            if (newEntry.priority(accessCounter) <= lowest.priority(accessCounter)) {
                return DEFAULT_NO_TOPIC_ALIAS;
            }
            if (++fullOverwriteTries < fullOverwriteCost) {
                return DEFAULT_NO_TOPIC_ALIAS;
            }
            fullOverwriteTries = 0;
            if (fullOverwriteCost < OVERWRITE_COST_MAX) {
                fullOverwriteCost += Math.min(OVERWRITE_COST_INC, OVERWRITE_COST_MAX - fullOverwriteCost);
            }
            if (lowest.topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                newEntry.setNewTopicAlias(lowest.topicAlias);
            }
            map.remove(lowest.topic);
            map.put(newEntry);
            final Entry higher = lowest.higher;
            newEntry.higher = higher;
            if (higher != null) {
                higher.lower = newEntry;
            }
        }
        lowest = newEntry;
        swapNewer(newEntry, accessCounter);
        return newEntry.topicAlias;
    }

    private void swapNewer(final @NotNull Entry entry, final long accessCounter) {
        Entry higher = entry.higher;
        if (entry.higher == null) {
            return;
        }
        Entry lower = entry.lower;
        final long priority = entry.priority(accessCounter);
        while (true) {
            final long newerPriority = higher.priority(accessCounter);
            if (newerPriority >= priority) {
                break;
            }
            if ((entry.topicAlias == DEFAULT_NO_TOPIC_ALIAS) && (higher.topicAlias != DEFAULT_NO_TOPIC_ALIAS)) {
                if (++overwriteTries < overwriteCost) {
                    break; // do not swap immediately if entry would overwrite the topic alias of the next entry
                }
                overwriteTries = 0;
                if (overwriteCost < OVERWRITE_COST_MAX) {
                    overwriteCost += Math.min(OVERWRITE_COST_INC, OVERWRITE_COST_MAX - overwriteCost);
                }
                entry.setNewTopicAlias(higher.topicAlias);
                higher.topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            }
            final Entry higherHigher = higher.higher;
            higher.higher = entry;
            entry.lower = higher;
            if (lower == null) {
                higher.lower = null;
                lowest = higher;
            } else {
                lower.higher = higher;
                higher.lower = lower;
            }
            if (higherHigher == null) {
                entry.higher = null;
                break;
            } else {
                entry.higher = higherHigher;
                higherHigher.lower = entry;
            }
            lower = higher;
            higher = higherHigher;
        }
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder builder = new StringBuilder("{");
        Entry entry = lowest;
        while (entry != null) {
            builder.append("\n  ").append(entry);
            entry = entry.higher;
        }
        return builder.append("\n}").toString();
    }

    static class Entry {

        final @NotNull String topic;
        int topicAlias;
        private long used; // number of accesses, decays over time
        private long access; // stamp when the entry was last accessed
        @Nullable Entry higher; // entry with the next higher priority
        @Nullable Entry lower; // entry with the next lower priority

        Entry(final @NotNull String topic, final long accessCounter) {
            this.topic = topic;
            topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            used = 1;
            access = accessCounter;
        }

        void setNewTopicAlias(final int topicAlias) {
            this.topicAlias = topicAlias | TOPIC_ALIAS_FLAG_NEW;
        }

        void access(final long accessCounter) {
            topicAlias &= TOPIC_ALIAS_FLAG; // clear NEW_TOPIC_ALIAS bit
            used = priority(accessCounter) + 1;
            access = accessCounter;
        }

        long priority(final long accessCounter) {
            final long decay = Math.max(accessCounter - access - RETAIN, 0);
            return Math.max(used - decay, 0);
        }

        @Override
        public @NotNull String toString() {
            return '{' + "topic='" + topic + '\'' +
                    ((topicAlias == DEFAULT_NO_TOPIC_ALIAS) ? "" : (", alias=" + (topicAlias & TOPIC_ALIAS_FLAG))) +
                    (((topicAlias & TOPIC_ALIAS_FLAG_NEW) == 0) ? "" : ", new ") + ", used = " + used + ", access = " +
                    access + '}';
        }
    }
}
