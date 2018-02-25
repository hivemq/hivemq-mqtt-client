package org.mqttbee.mqtt.message.subscribe;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscribeImpl extends
        MqttWrappedMessage<MqttSubscribeImpl, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
        implements Mqtt5Subscribe {

    private final ImmutableList<MqttSubscriptionImpl> subscriptions;

    public MqttSubscribeImpl(
            @NotNull final ImmutableList<MqttSubscriptionImpl> subscriptions,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttSubscribeImpl, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.subscriptions = subscriptions;
    }

    @NotNull
    @Override
    public ImmutableList<MqttSubscriptionImpl> getSubscriptions() {
        return subscriptions;
    }

    @NotNull
    @Override
    protected MqttSubscribeImpl getCodable() {
        return this;
    }

    public MqttSubscribeWrapper wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttSubscribeWrapper(this, packetIdentifier, subscriptionIdentifier);
    }

}
