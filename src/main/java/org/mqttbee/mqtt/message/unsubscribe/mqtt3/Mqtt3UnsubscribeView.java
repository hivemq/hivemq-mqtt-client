package org.mqttbee.mqtt.message.unsubscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3UnsubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3UnsubscribeView implements Mqtt3Unsubscribe {

    public static MqttUnsubscribeImpl wrapped(@NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new MqttUnsubscribeImpl(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt3UnsubscribeEncoder.PROVIDER);
    }

    public static Mqtt3UnsubscribeView create(@NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new Mqtt3UnsubscribeView((wrapped(topicFilters)));
    }

    private final MqttUnsubscribeImpl wrapped;

    private Mqtt3UnsubscribeView(@NotNull final MqttUnsubscribeImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return wrapped.getTopicFilters();
    }

    @NotNull
    public MqttUnsubscribeImpl getWrapped() {
        return wrapped;
    }

}
