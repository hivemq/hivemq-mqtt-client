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
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.HandleList.Handle;
import com.hivemq.client.internal.util.collections.Index;
import com.hivemq.client.internal.util.collections.NodeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
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
            final @NotNull MqttSubscription subscription, final int subscriptionIdentifier,
            final @Nullable MqttSubscribedPublishFlow flow) {

        final TopicTreeEntry entry = new TopicTreeEntry(subscription, subscriptionIdentifier, flow);
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(subscription.getTopicFilter());
        TopicTreeNode node = rootNode;
        if (node == null) {
            rootNode = node = new TopicTreeNode(null, null);
        }
        while (node != null) {
            node = node.subscribe(topicIterator, entry);
        }
    }

    @Override
    public void suback(
            final @NotNull MqttTopicFilterImpl topicFilter, final int subscriptionIdentifier, final boolean error) {

        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.suback(topicIterator, subscriptionIdentifier, error);
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
        for (Handle<MqttTopicFilterImpl> h = flow.getTopicFilters().getFirst(); h != null; h = h.getNext()) {
            final MqttTopicIterator topicIterator = MqttTopicIterator.of(h.getElement());
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

    private static class TopicTreeEntry extends NodeList.Node<TopicTreeEntry> {

        final int subscriptionIdentifier;
        final byte subscriptionOptions;
        @Nullable MqttSubscribedPublishFlow flow;
        @Nullable Handle<MqttTopicFilterImpl> handle;
        boolean acknowledged;

        TopicTreeEntry(
                final @NotNull MqttSubscription subscription, final int subscriptionIdentifier,
                final @Nullable MqttSubscribedPublishFlow flow) {

            this.subscriptionIdentifier = subscriptionIdentifier;
            subscriptionOptions = subscription.encodeSubscriptionOptions();
            this.flow = flow;
            handle = (flow == null) ? null : flow.getTopicFilters().add(subscription.getTopicFilter());
        }
    }

    private static class TopicTreeNode {

        private static final @NotNull Index.Spec<TopicTreeNode, MqttTopicLevel> INDEX_SPEC =
                new Index.Spec<>(node -> node.topicLevel, 4);

        private @Nullable TopicTreeNode parent;
        private @Nullable MqttTopicLevel topicLevel;
        private @Nullable Index<TopicTreeNode, MqttTopicLevel> next;
        private @Nullable TopicTreeNode singleLevel;
        private @Nullable NodeList<TopicTreeEntry> entries;
        private @Nullable NodeList<TopicTreeEntry> multiLevelEntries;

        TopicTreeNode(final @Nullable TopicTreeNode parent, final @Nullable MqttTopicLevel topicLevel) {
            this.parent = parent;
            this.topicLevel = topicLevel;
        }

        @Nullable TopicTreeNode subscribe(
                final @NotNull MqttTopicIterator topicIterator, final @NotNull TopicTreeEntry entry) {

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
                    next = new Index<>(INDEX_SPEC);
                    node = null;
                } else {
                    node = next.get(nextLevel);
                }
                if (node == null) {
                    node = new TopicTreeNode(this, nextLevel.trim());
                    next.put(node);
                    return node;
                }
                return getNext(node, topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (multiLevelEntries == null) {
                    multiLevelEntries = new NodeList<>();
                }
                multiLevelEntries.add(entry);
            } else {
                if (entries == null) {
                    entries = new NodeList<>();
                }
                entries.add(entry);
            }
            return null;
        }

        @Nullable TopicTreeNode suback(
                final @NotNull MqttTopicIterator topicIterator, final int subscriptionIdentifier, final boolean error) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (suback(multiLevelEntries, subscriptionIdentifier, error)) {
                    multiLevelEntries = null;
                }
            } else {
                if (suback(entries, subscriptionIdentifier, error)) {
                    entries = null;
                }
            }
            compact();
            return null;
        }

        private static boolean suback(
                final @Nullable NodeList<TopicTreeEntry> entries, final int subscriptionIdentifier,
                final boolean error) {

            if (entries != null) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.subscriptionIdentifier == subscriptionIdentifier) {
                        if (!error) {
                            entry.acknowledged = true;
                            return false;
                        }
                        if (entry.flow != null) {
                            assert entry.handle != null : "entry.flow != null -> entry.handle != null";
                            entry.flow.getTopicFilters().remove(entry.handle);
                        }
                        entries.remove(entry);
                        return entries.isEmpty();
                    }
                }
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
                if (unsubscribe(multiLevelEntries, unsubscribedCallback)) {
                    multiLevelEntries = null;
                }
            } else {
                if (unsubscribe(entries, unsubscribedCallback)) {
                    entries = null;
                }
            }
            compact();
            return null;
        }

        private static boolean unsubscribe(
                final @Nullable NodeList<TopicTreeEntry> entries,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (entries != null) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.acknowledged) {
                        if (entry.flow != null) {
                            assert entry.handle != null : "entry.flow != null -> entry.handle != null";
                            entry.flow.getTopicFilters().remove(entry.handle);
                            if (entry.flow.getTopicFilters().isEmpty()) {
                                entry.flow.onComplete();
                                if (unsubscribedCallback != null) {
                                    unsubscribedCallback.accept(entry.flow);
                                }
                            }
                        }
                        entries.remove(entry);
                    }
                }
                return entries.isEmpty();
            }
            return false;
        }

        @Nullable TopicTreeNode cancel(
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttSubscribedPublishFlow flow) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                cancel(multiLevelEntries, flow);
            } else {
                cancel(entries, flow);
            }
            return null;
        }

        private static void cancel(
                final @Nullable NodeList<TopicTreeEntry> entries, final @NotNull MqttSubscribedPublishFlow flow) {

            if (entries != null) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.flow == flow) {
                        entry.flow = null;
                        entry.handle = null;
                        break;
                    }
                }
            }
        }

        @Nullable TopicTreeNode findMatching(
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttMatchingPublishFlows matchingFlows) {

            if (topicIterator.hasNext()) {
                add(matchingFlows, multiLevelEntries);
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
            return null;
        }

        private static void add(
                final @NotNull MqttMatchingPublishFlows matchingFlows,
                final @Nullable NodeList<TopicTreeEntry> source) {

            if (source != null) {
                matchingFlows.subscriptionFound = true;
                for (TopicTreeEntry entry = source.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.flow != null) {
                        matchingFlows.add(entry.flow);
                    }
                }
            }
        }

        @Nullable TopicTreeNode clear(final @NotNull Throwable cause) {
            if (next != null) {
                return next.any();
            }
            if (singleLevel != null) {
                return singleLevel;
            }
            if (entries != null) {
                clear(entries, cause);
                entries = null;
            }
            if (multiLevelEntries != null) {
                clear(multiLevelEntries, cause);
                multiLevelEntries = null;
            }
            if (parent != null) {
                parent.removeNext(this);
            }
            return parent;
        }

        private static void clear(final @NotNull NodeList<TopicTreeEntry> entries, final @NotNull Throwable cause) {
            for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                if ((entry.flow != null) && entry.acknowledged) {
                    entry.flow.onError(cause);
                }
            }
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
                    if (topicLevelBefore.isSingleLevelWildcard()) {
                        singleLevel = nodeBefore;
                    } else {
                        assert next != null;
                        next.put(nodeBefore);
                    }
                    node.parent = nodeBefore;
                    node.topicLevel = topicLevelAfter;
                    if (topicLevelAfter.isSingleLevelWildcard()) {
                        nodeBefore.singleLevel = node;
                    } else {
                        nodeBefore.next = new Index<>(INDEX_SPEC);
                        nodeBefore.next.put(node);
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
            if ((parent != null) && (entries == null) && (multiLevelEntries == null)) {
                final boolean hasSingleLevel = singleLevel != null;
                final boolean hasNext = next != null;
                if (!hasSingleLevel && !hasNext) {
                    parent.removeNext(this);
                    parent.compact();
                } else if (hasSingleLevel && !hasNext) {
                    fuse(singleLevel);
                } else if (!hasSingleLevel && next.size() == 1) {
                    fuse(next.any());
                }
            }
        }

        private void fuse(final @NotNull TopicTreeNode child) {
            assert parent != null;
            assert topicLevel != null;
            assert child.parent == this;
            assert child.topicLevel != null;
            final TopicTreeNode parent = this.parent;
            final MqttTopicLevels fusedTopicLevel = MqttTopicLevels.concat(topicLevel, child.topicLevel);
            child.parent = parent;
            child.topicLevel = fusedTopicLevel;
            if (fusedTopicLevel.isSingleLevelWildcard()) {
                parent.singleLevel = child;
            } else {
                assert parent.next != null;
                parent.next.put(child);
            }
        }

        private void removeNext(final @NotNull TopicTreeNode node) {
            assert node.topicLevel != null;
            if (node.topicLevel.isSingleLevelWildcard()) {
                singleLevel = null;
            } else {
                assert next != null;
                next.remove(node.topicLevel);
                if (next.size() == 0) {
                    next = null;
                }
            }
        }

        boolean isEmpty() {
            return (next == null) && (singleLevel == null) && (entries == null) && (multiLevelEntries == null);
        }
    }
}
