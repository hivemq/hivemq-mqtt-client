package org.mqttbee.mqtt.message.publish.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3PublishEncoder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PublishView implements Mqtt3Publish {

    public static MqttPublish wrapped(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain) {

        return new MqttPublish(topic, payload, qos, isRetain, MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null,
                null, null, TopicAliasUsage.MUST_NOT, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt3PublishEncoder.PROVIDER);
    }

    public static MqttPublishWrapper wrapped(
            @NotNull final MqttPublish publish, final int packetIdentifier, final boolean isDup) {

        return publish.wrap(packetIdentifier, isDup, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false,
                MqttPublishWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    public static Mqtt3PublishView create(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain) {

        return new Mqtt3PublishView(wrapped(topic, payload, qos, isRetain));
    }

    private final MqttPublish wrapped;

    public Mqtt3PublishView(@NotNull final MqttPublish wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public MqttTopic getTopic() {
        return wrapped.getTopic();
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPayload() {
        return wrapped.getPayload();
    }

    @NotNull
    @Override
    public MqttQoS getQos() {
        return wrapped.getQos();
    }

    @Override
    public boolean isRetain() {
        return wrapped.isRetain();
    }

    @NotNull
    public MqttPublish getWrapped() {
        return wrapped;
    }

    @NotNull
    public MqttWillPublish getWrappedWill() {
        if (wrapped instanceof MqttWillPublish) {
            return (MqttWillPublish) wrapped;
        }
        return (MqttWillPublish) Mqtt5WillPublish.extend(wrapped).build();
    }

}
