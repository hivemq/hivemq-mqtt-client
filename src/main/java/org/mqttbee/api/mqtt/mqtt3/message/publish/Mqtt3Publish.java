package org.mqttbee.api.mqtt.mqtt3.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeResult;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * MQTT 3 PUBLISH packet.
 */
@DoNotImplement
public interface Mqtt3Publish extends Mqtt3Message, Mqtt3SubscribeResult {

    @NotNull
    static Mqtt3PublishBuilder builder() {
        return new Mqtt3PublishBuilder();
    }

    @NotNull
    static Mqtt3PublishBuilder extend(@NotNull final Mqtt3Publish publish) {
        return new Mqtt3PublishBuilder(publish);
    }

    /**
     * @return the topic of this PUBLISH packet.
     */
    @NotNull
    MqttTopic getTopic();

    /**
     * @return the optional payload of this PUBLISH packet.
     */
    @NotNull
    Optional<ByteBuffer> getPayload();

    /**
     * @return the QoS of this PUBLISH packet.
     */
    @NotNull
    MqttQoS getQos();

    /**
     * @return whether this PUBLISH packet is a retained message.
     */
    boolean isRetain();

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.PUBLISH;
    }


}
