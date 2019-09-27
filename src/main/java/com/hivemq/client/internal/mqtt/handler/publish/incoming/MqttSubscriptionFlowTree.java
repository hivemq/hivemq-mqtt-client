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

    private @Nullable TopicTreeNode rootNode;

    @Inject
    MqttSubscriptionFlowTree() {}

    @Override
    public void subscribe(
            final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {

        final TopicTreeEntry entry = (flow == null) ? null : new TopicTreeEntry(flow, topicFilter);
        if (rootNode == null) {
            rootNode = new TopicTreeNode(null, null);
        }
        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.subscribe(level.next(), entry);
        }
    }

    @Override
    public void remove(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        TopicTreeNode node = rootNode;
        TopicTreeNode lastNode = null;
        while (node != null) {
            lastNode = node;
            node = node.remove(level.next(), flow);
        }
        compact(lastNode);
    }

    @Override
    public void unsubscribe(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        TopicTreeNode node = rootNode;
        TopicTreeNode lastNode = null;
        while (node != null) {
            lastNode = node;
            node = node.unsubscribe(level.next(), unsubscribedCallback);
        }
        compact(lastNode);
    }

    private void compact(@Nullable TopicTreeNode lastNode) {
        while ((lastNode != null) && lastNode.isEmpty()) {
            final TopicTreeNode parentNode = lastNode.parentNode;
            if (parentNode == null) {
                rootNode = null;
            } else {
                parentNode.removeNext(lastNode);
            }
            lastNode = parentNode;
        }
    }

    @Override
    public void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
        for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
            final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
            TopicTreeNode node = rootNode;
            while (node != null) {
                node = node.cancel(level.next(), flow);
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
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.clear(cause);
        }
        rootNode = null;
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

        private final @Nullable TopicTreeNode parentNode;
        private final @Nullable ByteArray parentLevel;
        private @Nullable HashMap<ByteArray, TopicTreeNode> next;
        private @Nullable TopicTreeNode singleLevel;
        private @Nullable HandleList<TopicTreeEntry> multiLevelEntries;
        private @Nullable HandleList<TopicTreeEntry> entries;
        private int subscriptions;
        private int multiLevelSubscriptions;

        TopicTreeNode(final @Nullable TopicTreeNode parentNode, final @Nullable ByteArray parentLevel) {
            this.parentNode = parentNode;
            this.parentLevel = parentLevel;
        }

        @Nullable TopicTreeNode subscribe(final @Nullable MqttTopicLevel level, final @Nullable TopicTreeEntry entry) {
            if (level == null) {
                if (entry != null) {
                    if (entries == null) {
                        entries = new HandleList<>();
                    }
                    entries.add(entry);
                }
                subscriptions++;
                return null;
            }
            if (level.isMultiLevelWildcard()) {
                if (entry != null) {
                    if (multiLevelEntries == null) {
                        multiLevelEntries = new HandleList<>();
                    }
                    multiLevelEntries.add(entry);
                }
                multiLevelSubscriptions++;
                return null;
            }
            if (level.isSingleLevelWildcard()) {
                if (singleLevel == null) {
                    singleLevel = new TopicTreeNode(this, MqttTopicLevel.SINGLE_LEVEL_WILDCARD);
                }
                return singleLevel;
            }
            TopicTreeNode node;
            if (next == null) {
                next = new HashMap<>();
                node = null;
            } else {
                node = next.get(level);
            }
            if (node == null) {
                final ByteArray levelCopy = level.copy();
                node = new TopicTreeNode(this, levelCopy);
                next.put(levelCopy, node);
            }
            return node;
        }

        @Nullable TopicTreeNode remove(
                final @Nullable MqttTopicLevel level, final @Nullable MqttSubscribedPublishFlow flow) {

            if (level == null) {
                if (remove(entries, flow)) {
                    entries = null;
                }
                subscriptions--;
                return null;
            }
            if (level.isMultiLevelWildcard()) {
                if (remove(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
                multiLevelSubscriptions--;
                return null;
            }
            return traverseNext(level);
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

        @Nullable TopicTreeNode unsubscribe(
                final @Nullable MqttTopicLevel level,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (level == null) {
                unsubscribe(entries, unsubscribedCallback);
                entries = null;
                subscriptions = 0;
                return null;
            }
            if (level.isMultiLevelWildcard()) {
                unsubscribe(multiLevelEntries, unsubscribedCallback);
                multiLevelEntries = null;
                multiLevelSubscriptions = 0;
                return null;
            }
            return traverseNext(level);
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

        @Nullable TopicTreeNode cancel(
                final @Nullable MqttTopicLevel level, final @NotNull MqttSubscribedPublishFlow flow) {

            if (level == null) {
                if (cancel(entries, flow)) {
                    entries = null;
                }
                return null;
            }
            if (level.isMultiLevelWildcard()) {
                if (cancel(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
                return null;
            }
            return traverseNext(level);
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
                if (singleLevel != null) {
                    subscriptionFound |= singleLevel.findMatching(level.fork().next(), matchingFlows);
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

        @Nullable TopicTreeNode clear(final @NotNull Throwable cause) {
            if (next != null) {
                return next.values().iterator().next();
            }
            if (singleLevel != null) {
                return singleLevel;
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
            if (parentNode != null) {
                parentNode.removeNext(this);
            }
            return parentNode;
        }

        private @Nullable TopicTreeNode traverseNext(final @NotNull MqttTopicLevel level) {
            if (level.isSingleLevelWildcard()) {
                return singleLevel;
            }
            if (next != null) {
                return next.get(level);
            }
            return null;
        }

        private void removeNext(final @NotNull TopicTreeNode node) {
            assert next != null;
            assert node.parentLevel != null;
            if (node.parentLevel == MqttTopicLevel.SINGLE_LEVEL_WILDCARD) {
                singleLevel = null;
            } else {
                next.remove(node.parentLevel);
                if (next.isEmpty()) {
                    next = null;
                }
            }
        }

        boolean isEmpty() {
            return (subscriptions == 0) && (multiLevelSubscriptions == 0) && (singleLevel == null) && (next == null);
        }
    }
}
