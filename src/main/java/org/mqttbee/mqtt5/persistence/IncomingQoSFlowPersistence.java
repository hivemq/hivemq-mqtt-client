package org.mqttbee.mqtt5.persistence;

import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface IncomingQoSFlowPersistence {

    CompletableFuture<Void> persistPublish(int packetIdentifier);

    CompletableFuture<Void> persistPubRel(int packetIdentifier);

    CompletableFuture<Mqtt5MessageType> get(int packetIdentifier);

    CompletableFuture<Void> remove(int packetIdentifier);

}
