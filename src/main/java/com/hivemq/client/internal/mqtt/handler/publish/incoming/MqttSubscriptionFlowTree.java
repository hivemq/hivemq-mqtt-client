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

        final @NotNull MqttSubscribedPublishFlow flow;
        final @NotNull Handle<MqttTopicFilterImpl> handle;

        TopicTreeEntry(final @NotNull MqttSubscribedPublishFlow flow, final @NotNull MqttTopicFilterImpl topicFilter) {
            this.flow = flow;
            this.handle = flow.getTopicFilters().add(topicFilter);
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
                if (entry != null) {
                    if (multiLevelEntries == null) {
                        multiLevelEntries = new NodeList<>();
                    }
                    multiLevelEntries.add(entry);
                }
                multiLevelSubscriptions++;
            } else {
                if (entry != null) {
                    if (entries == null) {
                        entries = new NodeList<>();
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
            } else {
                if (remove(entries, flow)) {
                    entries = null;
                }
                subscriptions--;
            }
            compact();
            return null;
        }

        private static boolean remove(
                final @Nullable NodeList<TopicTreeEntry> entries, final @Nullable MqttSubscribedPublishFlow flow) {

            if ((entries != null) && (flow != null)) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.flow == flow) {
                        entry.flow.getTopicFilters().remove(entry.handle);
                        entries.remove(entry);
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
            } else {
                unsubscribe(entries, unsubscribedCallback);
                entries = null;
                subscriptions = 0;
            }
            compact();
            return null;
        }

        private static void unsubscribe(
                final @Nullable NodeList<TopicTreeEntry> entries,
                final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

            if (entries != null) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    final MqttSubscribedPublishFlow flow = entry.flow;
                    flow.getTopicFilters().remove(entry.handle);
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
                final @Nullable NodeList<TopicTreeEntry> entries, final @NotNull MqttSubscribedPublishFlow flow) {

            if (entries != null) {
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.flow == flow) {
                        entries.remove(entry);
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
                final @Nullable NodeList<TopicTreeEntry> source) {

            if (source != null) {
                for (TopicTreeEntry entry = source.getFirst(); entry != null; entry = entry.getNext()) {
                    target.add(entry.flow);
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
                entry.flow.onError(cause);
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
            if ((parent != null) && ((subscriptions + multiLevelSubscriptions) == 0)) {
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
            return ((subscriptions + multiLevelSubscriptions) == 0) && (singleLevel == null) && (next == null);
        }
    }
}
