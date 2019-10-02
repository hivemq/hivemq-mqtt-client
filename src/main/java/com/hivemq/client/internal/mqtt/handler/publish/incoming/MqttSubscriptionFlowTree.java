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
import com.hivemq.client.internal.mqtt.datatypes.*;
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
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
        TopicTreeNode node = rootNode;
        if (node == null) {
            rootNode = node = new TopicTreeNode(null, null);
        }
        while (node != null) {
            node = node.subscribe(topicIterator, entry);
        }
    }

    @Override
    public void remove(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.remove(topicIterator, flow);
        }
        compact();
    }

    @Override
    public void unsubscribe(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.unsubscribe(topicIterator, unsubscribedCallback);
        }
        compact();
    }

    @Override
    public void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
        for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
            final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
            TopicTreeNode node = rootNode;
            while (node != null) {
                node = node.cancel(topicIterator, flow);
            }
        }
    }

    @Override
    public void findMatching(
            final @NotNull MqttTopicImpl topic, final @NotNull MqttMatchingPublishFlows matchingFlows) {

        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topic);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.findMatching(topicIterator, matchingFlows);
        }
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.clear(cause);
        }
        rootNode = null;
    }

    private void compact() {
        if ((rootNode != null) && rootNode.isEmpty()) {
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

        private @Nullable TopicTreeNode parent;
        private @Nullable MqttTopicLevel topicLevel;
        private @Nullable HashMap<MqttTopicLevel, TopicTreeNode> next;
        private @Nullable TopicTreeNode singleLevel;
        private @Nullable HandleList<TopicTreeEntry> entries;
        private @Nullable HandleList<TopicTreeEntry> multiLevelEntries;
        private int subscriptions;
        private int multiLevelSubscriptions;

        TopicTreeNode(final @Nullable TopicTreeNode parent, final @Nullable MqttTopicLevel topicLevel) {
            this.parent = parent;
            this.topicLevel = topicLevel;
        }

        @Nullable TopicTreeNode subscribe(
                final @NotNull MqttTopicIterator topicIterator, final @Nullable TopicTreeEntry entry) {

            if (topicIterator.hasNext()) {
                final MqttTopicLevel nextLevel = topicIterator.next();
                if (nextLevel.isSingleLevelWildcard()) {
                    if (singleLevel == null) {
                        return singleLevel = new TopicTreeNode(this, nextLevel.trim());
                    }
                    return getNext(singleLevel, topicIterator);
                }
                TopicTreeNode node;
                if (next == null) {
                    next = new HashMap<>();
                    node = null;
                } else {
                    node = next.get(nextLevel);
                }
                if (node == null) {
                    node = new TopicTreeNode(this, nextLevel.trim());
                    next.put(node.topicLevel, node);
                    return node;
                }
                return getNext(node, topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (entry != null) {
                    if (multiLevelEntries == null) {
                        multiLevelEntries = new HandleList<>();
                    }
                    multiLevelEntries.add(entry);
                }
                multiLevelSubscriptions++;
            } else {
                if (entry != null) {
                    if (entries == null) {
                        entries = new HandleList<>();
                    }
                    entries.add(entry);
                }
                subscriptions++;
            }
            return null;
        }

        @Nullable TopicTreeNode remove(
                final @NotNull MqttTopicIterator topicIterator, final @Nullable MqttSubscribedPublishFlow flow) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (remove(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
                multiLevelSubscriptions--;
                compact();
            } else {
                if (remove(entries, flow)) {
                    entries = null;
                }
                subscriptions--;
                compact();
            }
            return null;
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
                final @NotNull MqttTopicIterator topicIterator,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                unsubscribe(multiLevelEntries, unsubscribedCallback);
                multiLevelEntries = null;
                multiLevelSubscriptions = 0;
                compact();
            } else {
                unsubscribe(entries, unsubscribedCallback);
                entries = null;
                subscriptions = 0;
                compact();
            }
            return null;
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
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttSubscribedPublishFlow flow) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (cancel(multiLevelEntries, flow)) {
                    multiLevelEntries = null;
                }
            } else {
                if (cancel(entries, flow)) {
                    entries = null;
                }
            }
            return null;
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

        @Nullable TopicTreeNode findMatching(
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttMatchingPublishFlows matchingFlows) {

            if (topicIterator.hasNext()) {
                add(matchingFlows, multiLevelEntries);
                if (multiLevelSubscriptions != 0) {
                    matchingFlows.subscriptionFound = true;
                }
                final MqttTopicLevel nextLevel = topicIterator.next();
                final TopicTreeNode nextNode = (next == null) ? null : next.get(nextLevel);
                final TopicTreeNode singleLevel = this.singleLevel;
                if (nextNode == null) {
                    return findNext(singleLevel, topicIterator);
                }
                if (singleLevel == null) {
                    return findNext(nextNode, topicIterator);
                }
                final MqttTopicIterator fork = topicIterator.fork();
                final TopicTreeNode nextNodeNext = findNext(nextNode, topicIterator);
                if (nextNodeNext == null) {
                    return findNext(singleLevel, topicIterator);
                }
                final TopicTreeNode singleLevelNext = findNext(singleLevel, fork);
                if (singleLevelNext == null) {
                    return nextNodeNext;
                }
                TopicTreeNode node = singleLevelNext;
                while (node != null) {
                    node = node.findMatching(fork, matchingFlows);
                }
                return nextNodeNext;
            }
            add(matchingFlows, entries);
            add(matchingFlows, multiLevelEntries);
            if ((subscriptions != 0) || (multiLevelSubscriptions != 0)) {
                matchingFlows.subscriptionFound = true;
            }
            return null;
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
            if (parent != null) {
                parent.removeNext(this);
            }
            return parent;
        }

        private @NotNull TopicTreeNode getNext(
                final @NotNull TopicTreeNode node, final @NotNull MqttTopicIterator topicIterator) {

            final MqttTopicLevel topicLevel = node.topicLevel;
            if (topicLevel instanceof MqttTopicLevels) {
                final MqttTopicLevels topicLevels = (MqttTopicLevels) topicLevel;
                final int branchIndex = topicIterator.forwardWhileEqual(topicLevels);
                final MqttTopicLevel topicLevelBefore = topicLevels.before(branchIndex);
                if (topicLevelBefore != topicLevels) {
                    final MqttTopicLevel topicLevelAfter = topicLevels.after(branchIndex);
                    final TopicTreeNode nodeBefore = new TopicTreeNode(this, topicLevelBefore);
                    node.parent = nodeBefore;
                    node.topicLevel = topicLevelAfter;
                    if (topicLevelBefore.isSingleLevelWildcard()) {
                        singleLevel = nodeBefore;
                    } else {
                        assert next != null;
                        next.remove(topicLevels);
                        next.put(topicLevelBefore, nodeBefore);
                    }
                    if (topicLevelAfter.isSingleLevelWildcard()) {
                        nodeBefore.singleLevel = node;
                    } else {
                        nodeBefore.next = new HashMap<>();
                        nodeBefore.next.put(topicLevelAfter, node);
                    }
                    return nodeBefore;
                }
            }
            return node;
        }

        private @Nullable TopicTreeNode traverseNext(final @NotNull MqttTopicIterator topicIterator) {
            final MqttTopicLevel nextLevel = topicIterator.next();
            if (nextLevel.isSingleLevelWildcard()) {
                return traverseNext(singleLevel, topicIterator);
            }
            if (next != null) {
                return traverseNext(next.get(nextLevel), topicIterator);
            }
            return null;
        }

        private static @Nullable TopicTreeNode traverseNext(
                final @Nullable TopicTreeNode node, final @NotNull MqttTopicIterator topicIterator) {

            if (node == null) {
                return null;
            }
            final MqttTopicLevel topicLevel = node.topicLevel;
            if (topicLevel instanceof MqttTopicLevels) {
                if (topicIterator.forwardIfEqual((MqttTopicLevels) topicLevel)) {
                    return node;
                }
                return null;
            }
            return node;
        }

        private static @Nullable TopicTreeNode findNext(
                final @Nullable TopicTreeNode node, final @NotNull MqttTopicIterator topicIterator) {

            if (node == null) {
                return null;
            }
            final MqttTopicLevel topicLevel = node.topicLevel;
            if (topicLevel instanceof MqttTopicLevels) {
                if (topicIterator.forwardIfMatch((MqttTopicLevels) topicLevel)) {
                    return node;
                }
                return null;
            }
            return node;
        }

        private void compact() {
            final TopicTreeNode parent = this.parent;
            if (isEmpty() && (parent != null)) {
                parent.removeNext(this);
                if ((parent.multiLevelSubscriptions + parent.subscriptions) == 0) {
                    if ((parent.singleLevel != null) && (parent.next == null)) {
                        parent.fuse(parent.singleLevel);
                    } else if ((parent.singleLevel == null) && (parent.next != null) && (parent.next.size() == 1)) {
                        parent.fuse(parent.next.values().iterator().next());
                    }
                }
            }
        }

        private void fuse(final @NotNull TopicTreeNode child) {
            assert child.parent == this;
            assert child.topicLevel != null;
            if (topicLevel != null) {
                topicLevel = MqttTopicLevels.concat(topicLevel, child.topicLevel);
                next = child.next;
                singleLevel = child.singleLevel;
                entries = child.entries;
                multiLevelEntries = child.multiLevelEntries;
                subscriptions = child.subscriptions;
                multiLevelSubscriptions = child.multiLevelSubscriptions;
            }
        }

        private void removeNext(final @NotNull TopicTreeNode node) {
            assert node.topicLevel != null;
            if (node.topicLevel.isSingleLevelWildcard()) {
                singleLevel = null;
            } else {
                assert next != null;
                next.remove(node.topicLevel);
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
