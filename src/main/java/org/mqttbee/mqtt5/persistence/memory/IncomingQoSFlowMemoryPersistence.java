package org.mqttbee.mqtt5.persistence.memory;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.IncomingQoSFlowPersistence;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class IncomingQoSFlowMemoryPersistence implements IncomingQoSFlowPersistence {

    private final Map<Integer, MqttPubRec> messages;

    @Inject
    IncomingQoSFlowMemoryPersistence() {
        messages = new HashMap<>();
    }

    @NotNull
    @Override
    public CompletableFuture<Void> store(@NotNull final MqttPubRec pubRec) {
        messages.put(pubRec.getPacketIdentifier(), pubRec);
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<MqttPubRec> get(final int packetIdentifier) {
        return CompletableFuture.completedFuture(messages.get(packetIdentifier));
    }

    @NotNull
    @Override
    public CompletableFuture<Void> discard(final int packetIdentifier) {
        messages.remove(packetIdentifier);
        return CompletableFuture.completedFuture(null);
    }

}
