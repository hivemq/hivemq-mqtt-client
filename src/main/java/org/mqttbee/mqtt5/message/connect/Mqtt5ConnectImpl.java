package org.mqttbee.mqtt5.message.connect;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.Mqtt5ExtendedAuth;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5ConnectEncoder;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectImpl extends Mqtt5Message.Mqtt5MessageWithProperties implements Mqtt5Connect {

    public static final int NOT_DEFAULT_RESPONSE_INFORMATION_REQUESTED = DEFAULT_RESPONSE_INFORMATION_REQUESTED ? 0 : 1;
    public static final int NOT_DEFAULT_PROBLEM_INFORMATION_REQUESTED = DEFAULT_PROBLEM_INFORMATION_REQUESTED ? 0 : 1;

    private final Mqtt5ClientIdentifier clientIdentifier;
    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final RestrictionsImpl restrictions;
    private final SimpleAuthImpl simpleAuth;
    private final Mqtt5ExtendedAuthImpl extendedAuth;
    private final Mqtt5WillPublishImpl willPublish;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    private int willPropertyLength = -1;

    public Mqtt5ConnectImpl(
            @NotNull final Mqtt5ClientIdentifier clientIdentifier, final int keepAlive, final boolean isCleanStart,
            final long sessionExpiryInterval, final boolean isResponseInformationRequested,
            final boolean isProblemInformationRequested, @NotNull final RestrictionsImpl restrictions,
            @Nullable final SimpleAuthImpl simpleAuth, @Nullable final Mqtt5ExtendedAuthImpl extendedAuth,
            @Nullable final Mqtt5WillPublishImpl willPublish,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.clientIdentifier = clientIdentifier;
        this.keepAlive = keepAlive;
        this.isCleanStart = isCleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.isResponseInformationRequested = isResponseInformationRequested;
        this.isProblemInformationRequested = isProblemInformationRequested;
        this.restrictions = restrictions;
        this.simpleAuth = simpleAuth;
        this.extendedAuth = extendedAuth;
        this.willPublish = willPublish;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Optional<Mqtt5ClientIdentifier> getClientIdentifier() {
        return clientIdentifier == Mqtt5ClientIdentifier.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER ? Optional.empty() :
                Optional.of(clientIdentifier);
    }

    @NotNull
    public Mqtt5ClientIdentifier getRawClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public boolean isCleanStart() {
        return isCleanStart;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return isResponseInformationRequested;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return isProblemInformationRequested;
    }

    @NotNull
    @Override
    public Restrictions getRestrictions() {
        return restrictions;
    }

    @NotNull
    public RestrictionsImpl getRawRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(simpleAuth);
    }

    @Nullable
    public SimpleAuthImpl getRawSimpleAuth() {
        return simpleAuth;
    }

    @NotNull
    @Override
    public Optional<Mqtt5ExtendedAuth> getExtendedAuth() {
        return Optional.ofNullable(extendedAuth);
    }

    @Nullable
    public Mqtt5ExtendedAuthImpl getRawExtendedAuth() {
        return extendedAuth;
    }

    @NotNull
    @Override
    public Optional<Mqtt5WillPublishImpl> getWillPublish() {
        return Optional.ofNullable(willPublish);
    }

    @Nullable
    public Mqtt5WillPublishImpl getRawWillPublish() {
        return willPublish;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5ConnectEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5ConnectEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5ConnectEncoder.INSTANCE.encodedPropertyLength(this);
    }

    public int encodedWillPropertyLength() {
        if (willPropertyLength == -1) {
            willPropertyLength = Mqtt5ConnectEncoder.INSTANCE.encodedWillPropertyLength(this);
        }
        return willPropertyLength;
    }


    public static class SimpleAuthImpl implements SimpleAuth {

        private final Mqtt5UTF8String username;
        private final byte[] password;

        public SimpleAuthImpl(@Nullable final Mqtt5UTF8String username, @Nullable final byte[] password) {
            this.username = username;
            this.password = password;
        }

        @NotNull
        @Override
        public Optional<Mqtt5UTF8String> getUsername() {
            return Optional.ofNullable(username);
        }

        @Nullable
        public Mqtt5UTF8String getRawUsername() {
            return username;
        }

        @NotNull
        @Override
        public Optional<byte[]> getPassword() {
            return Optional.ofNullable(password);
        }

        @Nullable
        public byte[] getRawPassword() {
            return password;
        }

    }


    public static class RestrictionsImpl implements Restrictions {

        @NotNull
        public static final RestrictionsImpl DEFAULT = new RestrictionsImpl(
                DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);

        private final int receiveMaximum;
        private final int topicAliasMaximum;
        private final long maximumPacketSize;

        public RestrictionsImpl(
                final int receiveMaximum, final int topicAliasMaximum, final long maximumPacketSize) {
            this.receiveMaximum = receiveMaximum;
            this.topicAliasMaximum = topicAliasMaximum;
            this.maximumPacketSize = maximumPacketSize;
        }

        @Override
        public int getReceiveMaximum() {
            return receiveMaximum;
        }

        @Override
        public int getTopicAliasMaximum() {
            return topicAliasMaximum;
        }

        @Override
        public long getMaximumPacketSize() {
            return maximumPacketSize;
        }

    }

}
