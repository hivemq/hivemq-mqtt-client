package org.mqttbee.mqtt5.persistence.memory;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class OutgoingQoSFlowMemoryPersistence implements OutgoingQoSFlowPersistence {

    private final IntMap<MqttQoSMessage> messages;

    @Inject
    OutgoingQoSFlowMemoryPersistence(final MqttClientData clientData) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;
        this.messages = new IntMap<>(clientConnectionData.getReceiveMaximum());
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
