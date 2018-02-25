package org.mqttbee.mqtt.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubscribe extends
        MqttWrappedMessage<MqttUnsubscribe, MqttUnsubscribeWrapper, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>>
        implements Mqtt5Unsubscribe {

    private final ImmutableList<MqttTopicFilterImpl> topicFilters;

    public MqttUnsubscribe(
            @NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttUnsubscribe, MqttUnsubscribeWrapper, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.topicFilters = topicFilters;
    }

    @NotNull
    @Override
    public ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    @NotNull
    @Override
    protected MqttUnsubscribe getCodable() {
        return this;
    }

    public MqttUnsubscribeWrapper wrap(final int packetIdentifier) {
        return new MqttUnsubscribeWrapper(this, packetIdentifier);
    }

}
