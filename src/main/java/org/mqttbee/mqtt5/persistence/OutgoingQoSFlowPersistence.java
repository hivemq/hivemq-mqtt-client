package org.mqttbee.mqtt5.persistence;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface OutgoingQoSFlowPersistence {

    CompletableFuture<Void> persist(@NotNull MqttPublishWrapper publishWrapper);

    CompletableFuture<Void> persist(@NotNull MqttPubRelImpl pubRel);

    CompletableFuture<MqttQoSMessage> get(int packetIdentifier);

    CompletableFuture<Void> remove(int packetIdentifier);

}
