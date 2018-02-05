package org.mqttbee.mqtt5.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.publish.Mqtt5TopicAliasMapping;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ServerData {

    private static final AttributeKey<Mqtt5ServerData> KEY = AttributeKey.valueOf("server.data");

    @NotNull
    public static Mqtt5ServerData get(@NotNull final Channel channel) {
        return Preconditions.checkNotNull(channel.attr(KEY).get());
    }

    public static int getMaximumPacketSize(@NotNull final Channel channel) {
        final Mqtt5ServerData serverData = channel.attr(KEY).get();
        return (serverData == null) ? Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT : serverData.getMaximumPacketSize();
    }

    private final int receiveMaximum;
    private final Mqtt5TopicAliasMapping topicAliasMapping;
    private final int maximumPacketSize;
    private final Mqtt5QoS maximumQoS;
    private final boolean isRetainAvailable;
    private final boolean isWildcardSubscriptionAvailable;
    private final boolean isSubscriptionIdentifierAvailable;
    private final boolean isSharedSubscriptionAvailable;

    public Mqtt5ServerData(
            final int receiveMaximum, final int maximumPacketSize, final int topicAliasMaximum,
            final Mqtt5QoS maximumQoS, final boolean isRetainAvailable, final boolean isWildcardSubscriptionAvailable,
            final boolean isSubscriptionIdentifierAvailable, final boolean isSharedSubscriptionAvailable) {
        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMapping = topicAliasMaximum == 0 ? null : new Mqtt5TopicAliasMapping(topicAliasMaximum);
        this.maximumQoS = maximumQoS;
        this.isRetainAvailable = isRetainAvailable;
        this.isWildcardSubscriptionAvailable = isWildcardSubscriptionAvailable;
        this.isSubscriptionIdentifierAvailable = isSubscriptionIdentifierAvailable;
        this.isSharedSubscriptionAvailable = isSharedSubscriptionAvailable;
    }

    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.size();
    }

    @Nullable
    public Mqtt5TopicAliasMapping getTopicAliasMapping() {
        return topicAliasMapping;
    }

    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    public Mqtt5QoS getMaximumQoS() {
        return maximumQoS;
    }

    public boolean isRetainAvailable() {
        return isRetainAvailable;
    }

    public boolean isWildcardSubscriptionAvailable() {
        return isWildcardSubscriptionAvailable;
    }

    public boolean isSubscriptionIdentifierAvailable() {
        return isSubscriptionIdentifierAvailable;
    }

    public boolean isSharedSubscriptionAvailable() {
        return isSharedSubscriptionAvailable;
    }

    public void set(@NotNull final Channel channel) {
        channel.attr(KEY).set(this);
    }

}
