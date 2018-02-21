package org.mqttbee.api.mqtt.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;

/**
 * Interface for providers for the QoS 1 control flow.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5QoS1ControlProvider {

    /**
     * Called when a server sent a Publish message with QoS 1.
     * <p>
     * This method must not block and just add some properties to the outgoing PubAck message.
     *
     * @param publish       the Publish message with QoS 1 sent by the server.
     * @param pubAckBuilder the builder for the outgoing PubAck message.
     */
    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubAckBuilder pubAckBuilder);

    /**
     * Called when a server sent a PubAck message for a Publish with QoS 1.
     * <p>
     * This method must not block.
     *
     * @param pubAck the PubAck message sent by the server.
     */
    void onPubAck(@NotNull Mqtt5PubAck pubAck);

}
