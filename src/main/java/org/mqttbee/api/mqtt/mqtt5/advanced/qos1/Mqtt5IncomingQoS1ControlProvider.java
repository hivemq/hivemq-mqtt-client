package org.mqttbee.api.mqtt.mqtt5.advanced.qos1;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;

/**
 * Interface for providers for controlling the QoS 1 control flow for incoming PUBLISH messages.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5IncomingQoS1ControlProvider {

    /**
     * Called when a server sent a PUBLISH message with QoS 1.
     * <p>
     * This method must not block and just add some properties to the outgoing PUBACK message.
     *
     * @param publish       the PUBLISH message with QoS 1 sent by the server.
     * @param pubAckBuilder the builder for the outgoing PUBACK message.
     */
    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubAckBuilder pubAckBuilder);

}
