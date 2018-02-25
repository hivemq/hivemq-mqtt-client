package org.mqttbee.api.mqtt3.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;

import java.util.Optional;

public interface Mqtt3Publish {

    /**
     * @return the topic of this PUBLISH packet.
     */
    @NotNull
    Mqtt5Topic getTopic();

    /**
     * @return the optional payload of this PUBLISH packet.
     */
    @NotNull
    Optional<byte[]> getPayload();

    /**
     * @return the QoS of this PUBLISH packet.
     */
    @NotNull
    Mqtt5QoS getQos();

    /**
     * @return whether this PUBLISH packet is a retained message.
     */
    boolean isRetained();

}
