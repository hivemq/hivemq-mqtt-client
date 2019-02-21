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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.NotThreadSafe;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicLevel;
import com.hivemq.client.internal.util.ByteArray;
import com.hivemq.client.internal.util.collections.HandleList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscriptionFlowTree implements MqttSubscriptionFlows {

    private static final @NotNull ByteArray ROOT_LEVEL = new ByteArray(new byte[0]);

    private @Nullable TopicTreeNode rootNode;

    @Inject
    MqttSubscriptionFlowTree() {}

    @Override
    public void subscribe(
            final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {

        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        final TopicTreeEntry entry = (flow == null) ? null : new TopicTreeEntry(flow, topicFilter);
        if (rootNode == null) {
            rootNode = new TopicTreeNode(ROOT_LEVEL, level, entry);
        } else {
            rootNode.subscribe(level, entry);
        }
    }

    @Override
    public void remove(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        if ((rootNode != null) && rootNode.remove(MqttTopicLevel.root(topicFilter), flow)) {
            rootNode = null;
        }
    }

    @Override
    public void unsubscribe(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

        if ((rootNode != null) && rootNode.unsubscribe(MqttTopicLevel.root(topicFilter), unsubscribedCallback)) {
            rootNode = null;
        }
    }

    @Override
    public void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
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

        final @NotNull MqttSubscribedPublishFlow flow;
        final @NotNull HandleList.Handle<MqttTopicFilterImpl> handle;

        TopicTreeEntry(final @NotNull MqttSubscribedPublishFlow flow, final @NotNull MqttTopicFilterImpl topicFilter) {
            this.flow = flow;
            this.handle = flow.getTopicFilters().add(topicFilter);
        }
    }

    private static class TopicTreeNode {

        private final @NotNull ByteArray parentLevel;
        private @Nullable HashMap<ByteArray, TopicTreeNode> next;
        private @Nullable HandleList<TopicTreeEntry> entries;
        private @Nullable HandleList<TopicTreeEntry> multiLevelEntries;
        private int subscriptions;
        private int multiLevelSubscriptions;
        private boolean hasSingleLevelSubscription;

        private TopicTreeNode(
                final @NotNull ByteArray parentLevel, final @Nullable MqttTopicLevel level,
                final @Nullable TopicTreeEntry entry) {

            this.parentLevel = parentLevel;
            subscribe(level, entry);
        }

        void subscribe(final @Nullable MqttTopicLevel level, final @Nullable TopicTreeEntry entry) {
            if (level == null) {
                if (entry != null) {
                    if (entries == null) {
                        entries = new HandleList<>();
                    }
                    entries.add(entry);
                }
                subscriptions++;
            } else if (level.isMultiLevelWildcard()) {
                if (entry != null) {
                    if (multiLevelEntries == null) {
                        multiLevelEntries = new HandleList<>();
                    }
                    multiLevelEntries.add(entry);
                }
                multiLevelSubscriptions++;
            } else {
                final TopicTreeNode node;
                if (next == null) {
                    next = new HashMap<>();
                    node = null;
                } else {
                    node = next.get(level);
                }
                if (node == null) {
                    if (level.isSingleLevelWildcard()) {
                        hasSingleLevelSubscription = true;
                    }
                    final ByteArray levelCopy = level.copy();
                    next.put(levelCopy, new TopicTreeNode(levelCopy, level.next(), entry));
                } else {
                    node.subscribe(level.next(), entry);
                }
            }
        }

        boolean remove(final @Nullable MqttTopicLevel level, final @Nullable MqttSubscribedPublishFlow flow) {
            if (level == null) {
                if (remove(entries, flow)) {
                    entries = null;
                }
                subscriptions--;
                return (subscriptions == 0) && (multiLevelSubscriptions == 0) && (next == null);
            }
            if (level.isMultiLevelWildcard()) {
                if (remove(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
                multiLevelSubscriptions--;
                return (subscriptions == 0) && (multiLevelSubscriptions == 0) && (next == null);
            }
            if (next != null) {
                final TopicTreeNode node = next.get(level);
                if ((node != null) && node.remove(level.next(), flow)) {
                    return removeNext(node);
                }
            }
            return false;
        }

        private static boolean remove(
                final @Nullable HandleList<TopicTreeEntry> entries, final @Nullable MqttSubscribedPublishFlow flow) {

            if ((entries != null) && (flow != null)) {
                for (final Iterator<TopicTreeEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
                    final TopicTreeEntry entry = iterator.next();
                    if (entry.flow == flow) {
                        entry.handle.remove();
                        iterator.remove();
                        break;
                    }
                }
                return entries.isEmpty();
            }
            return false;
        }

        boolean unsubscribe(
                final @Nullable MqttTopicLevel level,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (level == null) {
                unsubscribe(entries, unsubscribedCallback);
                entries = null;
                subscriptions = 0;
                return (multiLevelSubscriptions == 0) && (next == null);
            }
            if (level.isMultiLevelWildcard()) {
                unsubscribe(multiLevelEntries, unsubscribedCallback);
                multiLevelEntries = null;
                multiLevelSubscriptions = 0;
                return (subscriptions == 0) && (next == null);
            }
            if (next != null) {
                final TopicTreeNode node = next.get(level);
                if ((node != null) && node.unsubscribe(level.next(), unsubscribedCallback)) {
                    return removeNext(node);
                }
            }
            return false;
        }

        private static void unsubscribe(
                final @Nullable HandleList<TopicTreeEntry> entries,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (entries != null) {
                for (final TopicTreeEntry entry : entries) {
                    entry.handle.remove();
                    final MqttSubscribedPublishFlow flow = entry.flow;
                    if (flow.getTopicFilters().isEmpty()) {
                        flow.onComplete();
                        if (unsubscribedCallback != null) {
                            unsubscribedCallback.accept(flow);
                        }
                    }
                }
            }
        }

        private boolean removeNext(final @NotNull TopicTreeNode node) {
            assert next != null;
            if (node.parentLevel == MqttTopicLevel.SINGLE_LEVEL_WILDCARD) {
                hasSingleLevelSubscription = false;
            }
            next.remove(node.parentLevel);
            if (next.isEmpty()) {
                next = null;
                return (subscriptions == 0) && (multiLevelSubscriptions == 0);
            }
            return false;
        }

        void cancel(final @Nullable MqttTopicLevel level, final @NotNull MqttSubscribedPublishFlow flow) {
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
                final @Nullable HandleList<TopicTreeEntry> entries, final @NotNull MqttSubscribedPublishFlow flow) {

            if (entries != null) {
                for (final Iterator<TopicTreeEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
                    final TopicTreeEntry entry = iterator.next();
                    if (entry.flow == flow) {
                        iterator.remove();
                        break;
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
                return (subscriptions != 0) || (multiLevelSubscriptions != 0);
            }
            add(matchingFlows, multiLevelEntries);
            boolean subscriptionFound = (multiLevelSubscriptions != 0);
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
            if (next != null) {
                next.values().forEach(node -> node.clear(cause));
                next = null;
            }
            subscriptions = 0;
            multiLevelSubscriptions = 0;
            hasSingleLevelSubscription = false;
        }
    }
}
