package org.mqttbee.mqtt5.persistence.memory;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class OutgoingQoSFlowMemoryPersistence implements OutgoingQoSFlowPersistence {

    private final Map<Integer, MqttQoSMessage> messages;

    @Inject
    OutgoingQoSFlowMemoryPersistence() {
        this.messages = new HashMap<>();
    }

    @NotNull
    @Override
    public CompletableFuture<Void> store(@NotNull final MqttPublishWrapper publishWrapper) {
        messages.put(publishWrapper.getPacketIdentifier(), publishWrapper);
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> store(@NotNull final MqttPubRel pubRel) {
        messages.put(pubRel.getPacketIdentifier(), pubRel);
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<MqttQoSMessage> get(final int packetIdentifier) {
        return CompletableFuture.completedFuture(messages.get(packetIdentifier));
    }

    @NotNull
    @Override
    public CompletableFuture<Void> discard(final int packetIdentifier) {
        messages.remove(packetIdentifier);
        return CompletableFuture.completedFuture(null);
    }

}
