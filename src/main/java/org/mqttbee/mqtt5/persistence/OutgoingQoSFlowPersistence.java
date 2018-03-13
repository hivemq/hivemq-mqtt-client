package org.mqttbee.mqtt5.persistence;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface OutgoingQoSFlowPersistence {

    @NotNull
    CompletableFuture<Void> store(@NotNull MqttPublishWrapper publishWrapper);

    @NotNull
    CompletableFuture<Void> store(@NotNull MqttPubRel pubRel);

    @NotNull
    CompletableFuture<MqttQoSMessage> get(int packetIdentifier);

    @NotNull
    CompletableFuture<Void> discard(int packetIdentifier);

}
