package org.mqttbee.mqtt5.persistence.memory;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
public class OutgoingMemoryQoSFlowPersistence implements OutgoingQoSFlowPersistence {

    private final Map<Integer, MqttQoSMessage> messages;

    @Inject
    OutgoingMemoryQoSFlowPersistence() {
        this.messages = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> persist(@NotNull final MqttPublishWrapper publishWrapper) {
        messages.put(publishWrapper.getPacketIdentifier(), publishWrapper);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> persist(@NotNull final MqttPubRel pubRel) {
        messages.put(pubRel.getPacketIdentifier(), pubRel);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<MqttQoSMessage> get(final int packetIdentifier) {
        return CompletableFuture.completedFuture(messages.get(packetIdentifier));
    }

    @Override
    public CompletableFuture<Void> remove(final int packetIdentifier) {
        messages.remove(packetIdentifier);
        return CompletableFuture.completedFuture(null);
    }

}
