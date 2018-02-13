package org.mqttbee.mqtt5.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientDataImpl implements Mqtt5ClientData {

    private static final AttributeKey<Mqtt5ClientDataImpl> KEY = AttributeKey.valueOf("client.data");

    @NotNull
    public static Mqtt5ClientDataImpl get(@NotNull final Channel channel) {
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

    public Mqtt5ClientDataImpl(
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
    @Override
    public Mqtt5ClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    void setClientIdentifier(@NotNull final Mqtt5ClientIdentifierImpl clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    void setKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.length;
    }

    @Nullable
    public Mqtt5TopicImpl[] getTopicAliasMapping() {
        return topicAliasMapping;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getAuthMethod() {
        return Optional.ofNullable(authMethod);
    }

    @Nullable
    public Mqtt5UTF8String getRawAuthMethod() {
        return authMethod;
    }

    @Override
    public boolean hasWillPublish() {
        return hasWillPublish;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    public Channel getChannel() {
        return channel;
    }

}
