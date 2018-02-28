package org.mqttbee.api.mqtt.mqtt5.advanced.qos2;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;

/**
 * Interface for providers for controlling the QoS 2 control flow of outgoing PUBLISH messages.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5OutgoingQoS2ControlProvider {

    /**
     * Called when a server sent a PUBREC message for a PUBLISH with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRel message.
     *
     * @param pubRec        the PUBREC message sent by the server.
     * @param pubRelBuilder the builder for the outgoing PUBREL message.
     */
    void onPubRec(@NotNull Mqtt5PubRec pubRec, @NotNull Mqtt5PubRelBuilder pubRelBuilder);

    /**
     * Called when a server sent a PUBCOMP message for a PUBLISH with QoS 2.
     * <p>
     * This method must not block.
     *
     * @param pubComp the PUBCOMP message sent by the server.
     */
    void onPubComp(@NotNull Mqtt5PubComp pubComp);

}
