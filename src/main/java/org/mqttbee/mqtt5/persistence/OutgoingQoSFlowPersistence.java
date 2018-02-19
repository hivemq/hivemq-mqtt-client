package org.mqttbee.mqtt5.persistence;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;
import org.mqttbee.mqtt5.message.publish.pubrel.Mqtt5PubRelImpl;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface OutgoingQoSFlowPersistence {

    CompletableFuture<Void> persist(@NotNull Mqtt5PublishWrapper publishWrapper);

    CompletableFuture<Void> persist(@NotNull Mqtt5PubRelImpl pubRel);

    CompletableFuture<Mqtt5QoSMessage> get(int packetIdentifier);

    CompletableFuture<Void> remove(int packetIdentifier);

}
