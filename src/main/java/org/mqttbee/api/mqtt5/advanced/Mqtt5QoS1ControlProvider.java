package org.mqttbee.api.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5QoS1ControlProvider {

    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubAckBuilder pubAckBuilder);

    void onPubAck(@NotNull Mqtt5PubAck pubAck);

}
