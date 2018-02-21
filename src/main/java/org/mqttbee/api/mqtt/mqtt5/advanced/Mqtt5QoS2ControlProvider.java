package org.mqttbee.api.mqtt.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;

/**
 * Interface for providers for the QoS 2 control flow.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5QoS2ControlProvider {

    /**
     * Called when a server sent a Publish message with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRec message.
     *
     * @param publish       the Publish message with QoS 2 sent by the server.
     * @param pubAckBuilder the builder for the outgoing PubRec message.
     */
    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubRecBuilder pubAckBuilder);

    /**
     * Called when a server sent a PubRec message for a Publish with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRel message.
     *
     * @param pubRec        the PubRec message sent by the server.
     * @param pubRelBuilder the builder for the outgoing PubRel message.
     */
    void onPubRec(@NotNull Mqtt5PubRec pubRec, @NotNull Mqtt5PubRelBuilder pubRelBuilder);

    /**
     * Called when a server sent a PubRel message for a Publish with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubComp message.
     *
     * @param pubRel         the PubRel message sent by the server.
     * @param pubCompBuilder the builder for the outgoing PubComp message.
     */
    void onPubRel(@NotNull Mqtt5PubRel pubRel, @NotNull Mqtt5PubCompBuilder pubCompBuilder);

    /**
     * Called when a server sent a PubComp message for a Publish with QoS 2.
     * <p>
     * This method must not block.
     *
     * @param pubComp the PubComp message sent by the server.
     */
    void onPubComp(@NotNull Mqtt5PubComp pubComp);

}
