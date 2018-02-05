package org.mqttbee.mqtt5.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientData {

    private static final AttributeKey<Mqtt5ClientData> KEY = AttributeKey.valueOf("client.data");

    @NotNull
    public static Mqtt5ClientData get(@NotNull final Channel channel) {
        return Preconditions.checkNotNull(channel.attr(KEY).get());
    }

    private Mqtt5ClientIdentifierImpl clientIdentifier;
    private int keepAlive;
    private long sessionExpiryInterval;
    private final int receiveMaximum;
    private final Mqtt5TopicImpl[] topicAliasMapping;
    private final int maximumPacketSize;
    private final Mqtt5UTF8String authMethod;
    private final boolean hasWillPublish;
    private final boolean problemInformationRequested;
    private final Channel channel;

    public Mqtt5ClientData(
            @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier, final int keepAlive,
            final long sessionExpiryInterval, final int receiveMaximum, final int topicAliasMaximum,
            final int maximumPacketSize, final Mqtt5UTF8String authMethod, final boolean hasWillPublish,
            final boolean problemInformationRequested, @NotNull final Channel channel) {
        this.clientIdentifier = clientIdentifier;
        this.keepAlive = keepAlive;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMaximum = receiveMaximum;
        this.topicAliasMapping = (topicAliasMaximum == 0) ? null : new Mqtt5TopicImpl[topicAliasMaximum];
        this.maximumPacketSize = maximumPacketSize;
        this.authMethod = authMethod;
        this.hasWillPublish = hasWillPublish;
        this.problemInformationRequested = problemInformationRequested;
        this.channel = channel;

        channel.attr(KEY).set(this);
    }

    @NotNull
    public Mqtt5ClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    void setClientIdentifier(@NotNull final Mqtt5ClientIdentifierImpl clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    void setKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.length;
    }

    @Nullable
    public Mqtt5TopicImpl[] getTopicAliasMapping() {
        return topicAliasMapping;
    }

    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    public Mqtt5UTF8String getAuthMethod() {
        return authMethod;
    }

    public boolean hasWillPublish() {
        return hasWillPublish;
    }

    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    public Channel getChannel() {
        return channel;
    }

}
