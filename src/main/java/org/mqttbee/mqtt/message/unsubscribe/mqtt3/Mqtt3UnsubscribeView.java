package org.mqttbee.mqtt.message.unsubscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3UnsubscribeEncoder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3UnsubscribeView implements Mqtt3Unsubscribe {

    public static MqttUnsubscribe wrapped(@NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new MqttUnsubscribe(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt3UnsubscribeEncoder.PROVIDER);
    }

    public static Mqtt3UnsubscribeView create(@NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new Mqtt3UnsubscribeView((wrapped(topicFilters)));
    }

    private final MqttUnsubscribe wrapped;

    private Mqtt3UnsubscribeView(@NotNull final MqttUnsubscribe wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return wrapped.getTopicFilters();
    }

    @NotNull
    public MqttUnsubscribe getWrapped() {
        return wrapped;
    }

}
