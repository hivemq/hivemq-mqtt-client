package org.mqttbee.mqtt5.persistence;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;

import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public interface IncomingQoSFlowPersistence {

    @NotNull
    CompletableFuture<Void> store(@NotNull MqttPubRec pubRec);

    @NotNull
    CompletableFuture<MqttPubRec> get(int packetIdentifier);

    @NotNull
    CompletableFuture<Void> discard(int packetIdentifier);

}
