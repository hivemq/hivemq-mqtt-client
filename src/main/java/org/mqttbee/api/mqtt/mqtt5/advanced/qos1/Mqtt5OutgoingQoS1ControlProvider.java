package org.mqttbee.api.mqtt.mqtt5.advanced.qos1;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;

/**
 * Interface for providers for controlling the QoS 1 control flow for outgoing PUBLISH messages.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5OutgoingQoS1ControlProvider {

    /**
     * Called when a server sent a PUBACK message for a Publish with QoS 1.
     * <p>
     * This method must not block.
     *
     * @param pubAck the PUBACK message sent by the server.
     */
    void onPubAck(@NotNull Mqtt5PubAck pubAck);

}
