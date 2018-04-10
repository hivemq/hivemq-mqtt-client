package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscriptionFlowList implements MqttSubscriptionFlows {

    private final ScNodeList<MqttSubscriptionFlow> flows;
    private final HashSet<MqttTopicFilter> subscribedTopicFilters;

    @Inject
    MqttSubscriptionFlowList() {
        flows = new ScNodeList<>();
        subscribedTopicFilters = new HashSet<>();
    }

    @Override
    public void subscribe(@NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttSubscriptionFlow flow) {
        final ScNodeList<MqttTopicFilterImpl> topicFilters = flow.getTopicFilters();
        if (topicFilters.isEmpty()) {
            flows.add(flow);
        }
        topicFilters.add(topicFilter);
        subscribedTopicFilters.add(topicFilter);
    }

    @Override
    public void unsubscribe(
            @NotNull final MqttTopicFilterImpl topicFilter,
            @Nullable final Consumer<MqttSubscriptionFlow> unsubscribedCallback) {

        for (final MqttSubscriptionFlow flow : flows) {
            final ScNodeList<MqttTopicFilterImpl> flowTopicFilters = flow.getTopicFilters();
            for (final Iterator<MqttTopicFilterImpl> iterator = flowTopicFilters.iterator(); iterator.hasNext(); ) {
                final MqttTopicFilterImpl flowTopicFilter = iterator.next();
                if (topicFilter.matches(flowTopicFilter)) {
                    iterator.remove();
                    subscribedTopicFilters.remove(topicFilter);
                }
            }
            if (flowTopicFilters.isEmpty()) {
                flow.unsubscribe();
                if (unsubscribedCallback != null) {
                    unsubscribedCallback.accept(flow);
                }
            }
        }
        subscribedTopicFilters.removeIf(topicFilter::matches);
    }

    @Override
    public void cancel(@NotNull final MqttSubscriptionFlow flow) {
        for (final Iterator<MqttSubscriptionFlow> iterator = flows.iterator(); iterator.hasNext(); ) {
            final MqttSubscriptionFlow listFlow = iterator.next();
            if (listFlow == flow) {
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public boolean findMatching(
            @NotNull final MqttTopicImpl topic, @NotNull final ScNodeList<MqttIncomingPublishFlow> matchingFlows) {

        for (final MqttSubscriptionFlow flow : flows) {
            for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
                if (topicFilter.matches(topic)) {
                    MqttIncomingPublishFlows.addAndReference(matchingFlows, flow);
                    break;
                }
            }
        }
        if (!matchingFlows.isEmpty()) {
            return true;
        }
        for (final MqttTopicFilter subscribedTopicFilter : subscribedTopicFilters) {
            if (subscribedTopicFilter.matches(topic)) {
                return true;
            }
        }
        return false;
    }

}
