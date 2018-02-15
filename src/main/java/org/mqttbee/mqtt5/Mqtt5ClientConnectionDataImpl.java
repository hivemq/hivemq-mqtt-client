package org.mqttbee.mqtt5;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.Mqtt5ClientConnectionData;
import org.mqttbee.api.mqtt5.auth.Mqtt5ExtendedAuthProvider;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientConnectionDataImpl implements Mqtt5ClientConnectionData {

    private int keepAlive;
    private long sessionExpiryInterval;
    private final int receiveMaximum;
    private final Mqtt5TopicImpl[] topicAliasMapping;
    private final int maximumPacketSize;
    private final Mqtt5ExtendedAuthProvider extendedAuthProvider;
    private final boolean hasWillPublish;
    private final boolean problemInformationRequested;
    private final boolean responseInformationRequested;
    private final Channel channel;

    public Mqtt5ClientConnectionDataImpl(
            final int keepAlive, final long sessionExpiryInterval, final int receiveMaximum,
            final int topicAliasMaximum, final int maximumPacketSize,
            @Nullable final Mqtt5ExtendedAuthProvider extendedAuthProvider, final boolean hasWillPublish,
            final boolean problemInformationRequested, final boolean responseInformationRequested,
            @NotNull final Channel channel) {

        this.keepAlive = keepAlive;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMaximum = receiveMaximum;
        this.topicAliasMapping = (topicAliasMaximum == 0) ? null : new Mqtt5TopicImpl[topicAliasMaximum];
        this.maximumPacketSize = maximumPacketSize;
        this.extendedAuthProvider = extendedAuthProvider;
        this.hasWillPublish = hasWillPublish;
        this.problemInformationRequested = problemInformationRequested;
        this.responseInformationRequested = responseInformationRequested;
        this.channel = channel;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
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
        return (extendedAuthProvider == null) ? Optional.empty() : Optional.of(extendedAuthProvider.getMethod());
    }

    @Nullable
    public Mqtt5ExtendedAuthProvider getExtendedAuthProvider() {
        return extendedAuthProvider;
    }

    @Override
    public boolean hasWillPublish() {
        return hasWillPublish;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return responseInformationRequested;
    }

    public Channel getChannel() {
        return channel;
    }

}
