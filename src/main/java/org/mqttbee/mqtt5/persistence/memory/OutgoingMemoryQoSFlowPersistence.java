package org.mqttbee.mqtt5.persistence.memory;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;
import org.mqttbee.mqtt5.message.publish.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public class OutgoingMemoryQoSFlowPersistence implements OutgoingQoSFlowPersistence {

    private final Map<Integer, Mqtt5QoSMessage> messages;

    @Inject
    OutgoingMemoryQoSFlowPersistence() {
        this.messages = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> persist(@NotNull final Mqtt5PublishWrapper publishWrapper) {
        messages.put(publishWrapper.getPacketIdentifier(), publishWrapper);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> persist(@NotNull final Mqtt5PubRelImpl pubRel) {
        messages.put(pubRel.getPacketIdentifier(), pubRel);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Mqtt5QoSMessage> get(final int packetIdentifier) {
        return CompletableFuture.completedFuture(messages.get(packetIdentifier));
    }

    @Override
    public CompletableFuture<Void> remove(final int packetIdentifier) {
        messages.remove(packetIdentifier);
        return CompletableFuture.completedFuture(null);
    }

}
