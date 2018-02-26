package org.mqttbee.mqtt;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;

/**
 * @author Silvio Giebl
 */
public class MqttServerConnectionDataImpl implements Mqtt5ServerConnectionData {

    public static int getMaximumPacketSize(@NotNull final Channel channel) {
        final MqttServerConnectionDataImpl serverConnectionData =
                MqttClientDataImpl.from(channel).getRawServerConnectionData();
        return (serverConnectionData == null) ? MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT :
                serverConnectionData.getMaximumPacketSize();
    }

    @Nullable
    public static MqttTopicAliasMapping getTopicAliasMapping(@NotNull final Channel channel) {
        final MqttServerConnectionDataImpl serverConnectionData =
                MqttClientDataImpl.from(channel).getRawServerConnectionData();
        return (serverConnectionData == null) ? null : serverConnectionData.getTopicAliasMapping();
    }

    private final int receiveMaximum;
    private final MqttTopicAliasMapping topicAliasMapping;
    private final int maximumPacketSize;
    private final MqttQoS maximumQoS;
    private final boolean isRetainAvailable;
    private final boolean isWildcardSubscriptionAvailable;
    private final boolean isSubscriptionIdentifierAvailable;
    private final boolean isSharedSubscriptionAvailable;

    public MqttServerConnectionDataImpl(
            final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize,
            final MqttQoS maximumQoS, final boolean isRetainAvailable, final boolean isWildcardSubscriptionAvailable,
            final boolean isSubscriptionIdentifierAvailable, final boolean isSharedSubscriptionAvailable) {
        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMapping = topicAliasMaximum == 0 ? null : new MqttTopicAliasMapping(topicAliasMaximum);
        this.maximumQoS = maximumQoS;
        this.isRetainAvailable = isRetainAvailable;
        this.isWildcardSubscriptionAvailable = isWildcardSubscriptionAvailable;
        this.isSubscriptionIdentifierAvailable = isSubscriptionIdentifierAvailable;
        this.isSharedSubscriptionAvailable = isSharedSubscriptionAvailable;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.size();
    }

    @Nullable
    public MqttTopicAliasMapping getTopicAliasMapping() {
        return topicAliasMapping;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @NotNull
    @Override
    public MqttQoS getMaximumQoS() {
        return maximumQoS;
    }

    @Override
    public boolean isRetainAvailable() {
        return isRetainAvailable;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return isWildcardSubscriptionAvailable;
    }

    @Override
    public boolean isSubscriptionIdentifierAvailable() {
        return isSubscriptionIdentifierAvailable;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return isSharedSubscriptionAvailable;
    }

}
