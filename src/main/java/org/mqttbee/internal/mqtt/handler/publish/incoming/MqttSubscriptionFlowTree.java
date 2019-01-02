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

package org.mqttbee.internal.mqtt.handler.publish.incoming;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.annotations.NotThreadSafe;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicLevel;
import org.mqttbee.internal.util.ByteArray;
import org.mqttbee.internal.util.collections.HandleList;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscriptionFlowTree implements MqttSubscriptionFlows {

    private static final @NotNull ByteArray ROOT_LEVEL = new ByteArray(new byte[0], 0, 0);

    private @Nullable TopicTreeNode rootNode;

    @Inject
    MqttSubscriptionFlowTree() {
    }

    @Override
    public void subscribe(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscriptionFlow flow) {
        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        final TopicTreeEntry entry = (flow == null) ? null : new TopicTreeEntry(flow, topicFilter);
        if (rootNode == null) {
            rootNode = new TopicTreeNode(ROOT_LEVEL, level, entry);
        } else {
            rootNode.subscribe(level, entry);
        }
    }

    @Override
    public void unsubscribe(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @Nullable Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

        if (rootNode != null && rootNode.unsubscribe(MqttTopicLevel.root(topicFilter), unsubscribedCallback)) {
            rootNode = null;
        }
    }

    @Override
    public void cancel(final @NotNull MqttSubscriptionFlow flow) {
        if (rootNode != null) {
            for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
                rootNode.cancel(MqttTopicLevel.root(topicFilter), flow);
            }
        }
    }

    @Override
    public boolean findMatching(
            final @NotNull MqttTopicImpl topic, final @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows) {

        return (rootNode != null) && rootNode.findMatching(MqttTopicLevel.root(topic), matchingFlows);
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        if (rootNode != null) {
            rootNode.clear(cause);
            rootNode = null;
        }
    }

    private static class TopicTreeEntry {

        final @NotNull MqttSubscriptionFlow flow;
        final @NotNull HandleList.Handle<MqttTopicFilterImpl> handle;

        TopicTreeEntry(final @NotNull MqttSubscriptionFlow flow, final @NotNull MqttTopicFilterImpl topicFilter) {
            this.flow = flow;
            this.handle = flow.getTopicFilters().add(topicFilter);
        }

    }

    private static class TopicTreeNode {

        private final @NotNull ByteArray parentLevel;
        private @Nullable HashMap<ByteArray, TopicTreeNode> next;
        private @Nullable HandleList<TopicTreeEntry> entries;
        private @Nullable HandleList<TopicTreeEntry> multiLevelEntries;
        private boolean hasSubscription;
        private boolean hasSingleLevelSubscription;
        private boolean hasMultiLevelSubscription;

        private TopicTreeNode(
                final @NotNull ByteArray parentLevel, final @Nullable MqttTopicLevel level,
                final @Nullable TopicTreeEntry entry) {

            this.parentLevel = parentLevel;
            subscribe(level, entry);
        }

        void subscribe(final @Nullable MqttTopicLevel level, final @Nullable TopicTreeEntry entry) {
            if (level == null) {
                if (entries == null) {
                    entries = new HandleList<>();
                }
                if (entry != null) {
                    entries.add(entry);
                }
                hasSubscription = true;
            } else if (level.isMultiLevelWildcard()) {
                if (multiLevelEntries == null) {
                    multiLevelEntries = new HandleList<>();
                }
                if (entry != null) {
                    multiLevelEntries.add(entry);
                }
                hasMultiLevelSubscription = true;
            } else {
                final TopicTreeNode node;
                if (next == null) {
                    next = new HashMap<>();
                    node = null;
                } else {
                    node = next.get(level);
                }
                if (node == null) {
                    final ByteArray levelCopy;
                    if (level.isSingleLevelWildcard()) {
                        hasSingleLevelSubscription = true;
                        levelCopy = MqttTopicLevel.SINGLE_LEVEL_WILDCARD;
                    } else {
                        levelCopy = level.copy();
                    }
                    next.put(levelCopy, new TopicTreeNode(levelCopy, level.next(), entry));
                } else {
                    node.subscribe(level.next(), entry);
                }
            }
        }

        boolean unsubscribe(
                final @Nullable MqttTopicLevel level,
                final @Nullable Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

            if (level == null) {
                unsubscribe(entries, unsubscribedCallback);
                entries = null;
                hasSubscription = false;
                return (next == null) && (multiLevelEntries == null);
            }
            if (level.isMultiLevelWildcard()) {
                unsubscribe(multiLevelEntries, unsubscribedCallback);
                multiLevelEntries = null;
                hasMultiLevelSubscription = false;
                return (next == null) && (entries == null);
            }
            if (next != null) {
                final TopicTreeNode node = next.get(level);
                if ((node != null) && node.unsubscribe(level.next(), unsubscribedCallback)) {
                    if (node.parentLevel == MqttTopicLevel.SINGLE_LEVEL_WILDCARD) {
                        hasSingleLevelSubscription = false;
                    }
                    next.remove(node.parentLevel);
                    if (next.isEmpty()) {
                        next = null;
                        return (entries == null) && (multiLevelEntries == null);
                    }
                }
            }
            return false;
        }

        private static void unsubscribe(
                final @Nullable HandleList<TopicTreeEntry> entries,
                final @Nullable Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

            if (entries != null) {
                for (final TopicTreeEntry entry : entries) {
                    entry.handle.remove();
                    final MqttSubscriptionFlow flow = entry.flow;
                    if (flow.getTopicFilters().isEmpty()) {
                        flow.onComplete();
                        if (unsubscribedCallback != null) {
                            unsubscribedCallback.accept(flow);
                        }
                    }
                }
            }
        }

        void cancel(final @Nullable MqttTopicLevel level, final @NotNull MqttSubscriptionFlow flow) {
            if (level == null) {
                if (cancel(entries, flow)) {
                    entries = null;
                }
            } else if (level.isMultiLevelWildcard()) {
                if (cancel(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
            } else if (next != null) {
                final TopicTreeNode node = next.get(level);
                if (node != null) {
                    node.cancel(level.next(), flow);
                }
            }
        }

        private static boolean cancel(
                final @Nullable HandleList<TopicTreeEntry> entries, final @NotNull MqttSubscriptionFlow flow) {

            if (entries != null) {
                for (final Iterator<TopicTreeEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
                    final TopicTreeEntry entry = iterator.next();
                    if (entry.flow == flow) {
                        iterator.remove();
                    }
                }
                return entries.isEmpty();
            }
            return false;
        }

        boolean findMatching(
                final @Nullable MqttTopicLevel level,
                final @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows) {

            if (level == null) {
                add(matchingFlows, entries);
                add(matchingFlows, multiLevelEntries);
                return hasSubscription || hasMultiLevelSubscription;
            }
            add(matchingFlows, multiLevelEntries);
            boolean subscriptionFound = hasMultiLevelSubscription;
            if (next != null) {
                if (hasSingleLevelSubscription) {
                    final TopicTreeNode singleLevelNode = next.get(MqttTopicLevel.SINGLE_LEVEL_WILDCARD);
                    subscriptionFound |= singleLevelNode.findMatching(level.fork().next(), matchingFlows);
                }
                final TopicTreeNode node = next.get(level);
                if (node != null) {
                    subscriptionFound |= node.findMatching(level.next(), matchingFlows);
                }
            }
            return subscriptionFound;
        }

        private static void add(
                final @NotNull HandleList<MqttIncomingPublishFlow> target,
                final @Nullable HandleList<TopicTreeEntry> source) {

            if (source != null) {
                for (final TopicTreeEntry entry : source) {
                    target.add(entry.flow);
                }
            }
        }

        void clear(final @NotNull Throwable cause) {
            if (next != null) {
                next.values().forEach(node -> node.clear(cause));
                next = null;
            }
            if (entries != null) {
                for (final TopicTreeEntry entry : entries) {
                    entry.flow.onError(cause);
                }
                entries = null;
            }
            if (multiLevelEntries != null) {
                for (final TopicTreeEntry multiLevelEntry : multiLevelEntries) {
                    multiLevelEntry.flow.onError(cause);
                }
                multiLevelEntries = null;
            }
            hasSubscription = false;
            hasSingleLevelSubscription = false;
            hasMultiLevelSubscription = false;
        }

    }

}
