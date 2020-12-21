/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.annotations.NotThreadSafe;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicIterator;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicLevel;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicLevels;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client2.internal.util.collections.HandleList.Handle;
import com.hivemq.client2.internal.util.collections.Index;
import com.hivemq.client2.internal.util.collections.NodeList;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscribedPublishFlowTree implements MqttSubscribedPublishFlows {

    private @Nullable TopicTreeNode rootNode;

    MqttSubscribedPublishFlowTree() {}

    @Override
    public void subscribe(
            final @NotNull MqttSubscription subscription,
            final int subscriptionIdentifier,
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
            node = node.suback(topicIterator, topicFilter, subscriptionIdentifier, error);
        }
        compact();
    }

    @Override
    public void unsubscribe(final @NotNull MqttTopicFilterImpl topicFilter) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(topicFilter);
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.unsubscribe(topicIterator, topicFilter);
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
    public void findMatching(final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(publishWithFlows.publish.stateless().getTopic());
        TopicTreeNode node = rootNode;
        while (node != null) {
            node = node.findMatching(topicIterator, publishWithFlows);
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

    @Override
    public @NotNull Map<@NotNull Integer, @NotNull List<@NotNull MqttSubscription>> getSubscriptions() {
        // we sort in reverse order of subscription identifiers so that newer subscriptions are first
        final Map<Integer, List<MqttSubscription>> map = new TreeMap<>(Comparator.reverseOrder());
        if (rootNode != null) {
            final Queue<IteratorNode> nodes = new LinkedList<>();
            nodes.add(new IteratorNode(rootNode, null));
            while (!nodes.isEmpty()) {
                final IteratorNode node = nodes.poll();
                node.node.getSubscriptions(node.parentTopicLevels, map, nodes);
            }
        }
        return map;
    }

    private void compact() {
        if ((rootNode != null) && rootNode.isEmpty()) {
            rootNode = null;
        }
    }

    private static class TopicTreeEntry extends NodeList.Node<TopicTreeEntry> {

        final int subscriptionIdentifier;
        final byte subscriptionOptions;
        final byte @Nullable [] topicFilterPrefix;
        @Nullable MqttSubscribedPublishFlow flow;
        @Nullable Handle<MqttTopicFilterImpl> handle;
        boolean acknowledged;

        TopicTreeEntry(
                final @NotNull MqttSubscription subscription,
                final int subscriptionIdentifier,
                final @Nullable MqttSubscribedPublishFlow flow) {

            this.subscriptionIdentifier = subscriptionIdentifier;
            subscriptionOptions = subscription.encodeSubscriptionOptions();
            final MqttTopicFilterImpl topicFilter = subscription.getTopicFilter();
            this.topicFilterPrefix = topicFilter.getPrefix();
            this.flow = flow;
            handle = (flow == null) ? null : flow.getTopicFilters().add(topicFilter);
        }
    }

    private static class TopicTreeNode {

        private static final Index.@NotNull Spec<TopicTreeNode, MqttTopicLevel> INDEX_SPEC =
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
                final @NotNull MqttTopicIterator topicIterator,
                final @NotNull MqttTopicFilterImpl topicFilter,
                final int subscriptionIdentifier,
                final boolean error) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (suback(multiLevelEntries, topicFilter, subscriptionIdentifier, error)) {
                    multiLevelEntries = null;
                }
            } else {
                if (suback(entries, topicFilter, subscriptionIdentifier, error)) {
                    entries = null;
                }
            }
            compact();
            return null;
        }

        private static boolean suback(
                final @Nullable NodeList<TopicTreeEntry> entries,
                final @NotNull MqttTopicFilterImpl topicFilter,
                final int subscriptionIdentifier,
                final boolean error) {

            if (entries != null) {
                final byte[] topicFilterPrefix = topicFilter.getPrefix();
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if ((entry.subscriptionIdentifier == subscriptionIdentifier) &&
                            Arrays.equals(topicFilterPrefix, entry.topicFilterPrefix)) {
                        if (!error) {
                            entry.acknowledged = true;
                        } else {
                            if (entry.flow != null) {
                                assert entry.handle != null : "entry.flow != null -> entry.handle != null";
                                entry.flow.getTopicFilters().remove(entry.handle);
                            }
                            entries.remove(entry);
                        }
                    }
                }
                return entries.isEmpty();
            }
            return false;
        }

        @Nullable TopicTreeNode unsubscribe(
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttTopicFilterImpl topicFilter) {

            if (topicIterator.hasNext()) {
                return traverseNext(topicIterator);
            }
            if (topicIterator.hasMultiLevelWildcard()) {
                if (unsubscribe(multiLevelEntries, topicFilter)) {
                    multiLevelEntries = null;
                }
            } else {
                if (unsubscribe(entries, topicFilter)) {
                    entries = null;
                }
            }
            compact();
            return null;
        }

        private static boolean unsubscribe(
                final @Nullable NodeList<TopicTreeEntry> entries, final @NotNull MqttTopicFilterImpl topicFilter) {

            if (entries != null) {
                final byte[] topicFilterPrefix = topicFilter.getPrefix();
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (Arrays.equals(topicFilterPrefix, entry.topicFilterPrefix) && entry.acknowledged) {
                        if (entry.flow != null) {
                            assert entry.handle != null : "entry.flow != null -> entry.handle != null";
                            entry.flow.getTopicFilters().remove(entry.handle);
                            if (entry.flow.getTopicFilters().isEmpty()) {
                                entry.flow.onComplete();
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
                final @NotNull MqttTopicIterator topicIterator, final @NotNull MqttStatefulPublishWithFlows flows) {

            if (topicIterator.hasNext()) {
                add(flows, multiLevelEntries);
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
                    node = node.findMatching(fork, flows);
                }
                return nextNodeNext;
            }
            add(flows, entries);
            add(flows, multiLevelEntries);
            return null;
        }

        private static void add(
                final @NotNull MqttStatefulPublishWithFlows flows, final @Nullable NodeList<TopicTreeEntry> entries) {

            if (entries != null) {
                flows.subscriptionFound = true;
                for (TopicTreeEntry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    if (entry.flow != null) {
                        flows.add(entry.flow);
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
                        assert next != null : "node must be in next -> next != null";
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
            assert parent != null : "parent = null -> this = root node, root node must not be fused";
            assert topicLevel != null : "topicLevel = null -> this = root node, root node must not be fused";
            assert child.parent == this : "this must only be fused with its child";
            assert child.topicLevel != null : "child.topicLevel = null -> child = root node, root node has no parent";
            final TopicTreeNode parent = this.parent;
            final MqttTopicLevels fusedTopicLevel = MqttTopicLevels.concat(topicLevel, child.topicLevel);
            child.parent = parent;
            child.topicLevel = fusedTopicLevel;
            if (fusedTopicLevel.isSingleLevelWildcard()) {
                parent.singleLevel = child;
            } else {
                assert parent.next != null : "this must be in parent.next -> parent.next != null";
                parent.next.put(child);
            }
        }

        private void removeNext(final @NotNull TopicTreeNode node) {
            assert node.topicLevel != null : "topicLevel = null -> node = root node, root node has no parent";
            if (node.topicLevel.isSingleLevelWildcard()) {
                singleLevel = null;
            } else {
                assert next != null : "node must be in next -> next != null";
                next.remove(node.topicLevel);
                if (next.size() == 0) {
                    next = null;
                }
            }
        }

        boolean isEmpty() {
            return (next == null) && (singleLevel == null) && (entries == null) && (multiLevelEntries == null);
        }

        void getSubscriptions(
                final @Nullable MqttTopicLevel parentTopicLevels,
                final @NotNull Map<@NotNull Integer, @NotNull List<@NotNull MqttSubscription>> map,
                final @NotNull Queue<@NotNull IteratorNode> nodes) {

            final MqttTopicLevel topicLevels = ((parentTopicLevels == null) || (topicLevel == null)) ? topicLevel :
                    MqttTopicLevels.concat(parentTopicLevels, topicLevel);
            if (entries != null) {
                getSubscriptions(entries, topicLevels, false, map);
            }
            if (multiLevelEntries != null) {
                getSubscriptions(multiLevelEntries, topicLevels, true, map);
            }
            if (next != null) {
                next.forEach(node -> nodes.add(new IteratorNode(node, topicLevels)));
            }
            if (singleLevel != null) {
                nodes.add(new IteratorNode(singleLevel, topicLevels));
            }
        }

        private static void getSubscriptions(
                final @NotNull NodeList<TopicTreeEntry> entries,
                final @Nullable MqttTopicLevel topicLevels,
                final boolean multiLevelWildcard,
                final @NotNull Map<@NotNull Integer, @NotNull List<@NotNull MqttSubscription>> map) {

            // exact subscription = subscription without prefix, so no shared subscription
            boolean exactFound = false;
            // iterate in reverse order to only include the newest exact subscription
            for (TopicTreeEntry entry = entries.getLast(); entry != null; entry = entry.getPrev()) {
                if (entry.acknowledged) {
                    if (entry.topicFilterPrefix == null) {
                        if (exactFound) {
                            // ignore older exact subscriptions as they are overwritten by the newest
                            continue;
                        }
                        exactFound = true;
                    }
                    final MqttTopicFilterImpl topicFilter =
                            MqttTopicLevel.toFilter(entry.topicFilterPrefix, topicLevels, multiLevelWildcard);
                    assert topicFilter != null : "reconstructed topic filter must be valid";
                    final MqttQos maxQos = MqttSubscription.decodeMaxQos(entry.subscriptionOptions);
                    assert maxQos != null : "reconstructed maxQos must be valid";
                    final boolean noLocal = MqttSubscription.decodeNoLocal(entry.subscriptionOptions);
                    final Mqtt5RetainHandling retainHandling =
                            MqttSubscription.decodeRetainHandling(entry.subscriptionOptions);
                    assert retainHandling != null : "reconstructed retain handling must be valid";
                    final boolean retainAsPublished =
                            MqttSubscription.decodeRetainAsPublished(entry.subscriptionOptions);
                    final MqttSubscription subscription =
                            new MqttSubscription(topicFilter, maxQos, noLocal, retainHandling, retainAsPublished);
                    map.computeIfAbsent(entry.subscriptionIdentifier, k -> new LinkedList<>()).add(subscription);
                }
            }
        }
    }

    private static class IteratorNode {

        final @NotNull TopicTreeNode node;
        final @Nullable MqttTopicLevel parentTopicLevels;

        IteratorNode(final @NotNull TopicTreeNode node, final @Nullable MqttTopicLevel parentTopicLevels) {
            this.node = node;
            this.parentTopicLevels = parentTopicLevels;
        }
    }
}
