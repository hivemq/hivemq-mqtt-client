package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicLevel;
import org.mqttbee.util.ByteArray;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscriptionFlowTree implements MqttSubscriptionFlows {

    private static final ByteArray ROOT_LEVEL = new ByteArray(new byte[0], 0, 0);

    private TopicTreeNode rootNode;

    @Inject
    MqttSubscriptionFlowTree() {
    }

    @Override
    public void subscribe(@NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttSubscriptionFlow flow) {
        final MqttTopicLevel level = MqttTopicLevel.root(topicFilter);
        final TopicTreeEntry entry = new TopicTreeEntry(flow, topicFilter);
        if (rootNode == null) {
            rootNode = new TopicTreeNode(ROOT_LEVEL, level, entry);
        } else {
            rootNode.subscribe(level, entry);
        }
    }

    @Override
    public void unsubscribe(
            @NotNull final MqttTopicFilterImpl topicFilter,
            @Nullable final Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

        if (rootNode != null) {
            if (rootNode.unsubscribe(MqttTopicLevel.root(topicFilter), unsubscribedCallback)) {
                rootNode = null;
            }
        }
    }

    @Override
    public void cancel(@NotNull final MqttSubscriptionFlow flow) {
        if (rootNode != null) {
            for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
                rootNode.cancel(MqttTopicLevel.root(topicFilter), flow);
            }
        }
    }

    @Override
    public boolean findMatching(
            @NotNull final MqttTopicImpl topic, @NotNull final ScNodeList<MqttIncomingPublishFlow> matchingFlows) {

        return (rootNode != null) && rootNode.findMatching(MqttTopicLevel.root(topic), matchingFlows);
    }


    private static class TopicTreeEntry {

        private final MqttSubscriptionFlow flow;
        private final ScNodeList.Handle<MqttTopicFilterImpl> handle;

        private TopicTreeEntry(
                @NotNull final MqttSubscriptionFlow flow, @NotNull final MqttTopicFilterImpl topicFilter) {

            this.flow = flow;
            this.handle = flow.getTopicFilters().add(topicFilter);
        }

    }


    private static class TopicTreeNode {

        @NotNull
        private final ByteArray parentLevel;
        @Nullable
        private HashMap<ByteArray, TopicTreeNode> next;
        @Nullable
        private ScNodeList<TopicTreeEntry> entries;
        @Nullable
        private ScNodeList<TopicTreeEntry> multiLevelEntries;
        private boolean hasSubscription;
        private boolean hasSingleLevelSubscription;
        private boolean hasMultiLevelSubscription;

        private TopicTreeNode(
                @NotNull final ByteArray parentLevel, @Nullable final MqttTopicLevel level,
                @NotNull final TopicTreeEntry entry) {

            this.parentLevel = parentLevel;
            if (level == null) {
                entries = new ScNodeList<>();
                entries.add(entry);
            } else {
                next = new HashMap<>();
                final ByteArray levelCopy = level.copy();
                next.put(levelCopy, new TopicTreeNode(levelCopy, level.next(), entry));
            }
        }

        private void subscribe(@Nullable final MqttTopicLevel level, @NotNull final TopicTreeEntry entry) {
            if (level == null) {
                if (entries == null) {
                    entries = new ScNodeList<>();
                }
                entries.add(entry);
                hasSubscription = true;
            } else if (level.isMultiLevelWildcard()) {
                if (multiLevelEntries == null) {
                    multiLevelEntries = new ScNodeList<>();
                }
                multiLevelEntries.add(entry);
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

        private boolean unsubscribe(
                @Nullable final MqttTopicLevel level,
                @Nullable final Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

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
                if (node != null) {
                    if (node.unsubscribe(level.next(), unsubscribedCallback)) {
                        if (node.parentLevel.equals(MqttTopicLevel.SINGLE_LEVEL_WILDCARD)) {
                            hasSingleLevelSubscription = false;
                        }
                        next.remove(node.parentLevel);
                        if (next.isEmpty()) {
                            next = null;
                            return (entries == null) && (multiLevelEntries == null);
                        }
                    }
                }
            }
            return false;
        }

        private static void unsubscribe(
                @Nullable final ScNodeList<TopicTreeEntry> entries,
                @Nullable final Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

            if (entries != null) {
                for (final TopicTreeEntry entry : entries) {
                    if (entry.handle.remove()) {
                        final MqttSubscriptionFlow flow = entry.flow;
                        flow.unsubscribe();
                        if (unsubscribedCallback != null) {
                            unsubscribedCallback.accept(flow);
                        }
                    }
                }
            }
        }

        private void cancel(@Nullable final MqttTopicLevel level, @NotNull final MqttSubscriptionFlow flow) {
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
                @Nullable final ScNodeList<TopicTreeEntry> entries, @NotNull final MqttSubscriptionFlow flow) {

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

        private boolean findMatching(
                @Nullable final MqttTopicLevel level,
                @NotNull final ScNodeList<MqttIncomingPublishFlow> matchingFlows) {

            if (level == null) {
                addAndReference(matchingFlows, entries);
                addAndReference(matchingFlows, multiLevelEntries);
                return hasSubscription || hasMultiLevelSubscription;
            }
            addAndReference(matchingFlows, multiLevelEntries);
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

        private static void addAndReference(
                @NotNull final ScNodeList<MqttIncomingPublishFlow> target,
                @Nullable final ScNodeList<TopicTreeEntry> source) {

            if (source != null) {
                for (final TopicTreeEntry entry : source) {
                    entry.flow.reference();
                    target.add(entry.flow);
                }
            }
        }

    }

}
