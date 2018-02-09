package org.mqttbee.mqtt5.message.connect;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5ExtendedAuth;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithUserProperties;
import org.mqttbee.mqtt5.message.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.util.ByteBufUtil;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectImpl extends Mqtt5MessageWithUserProperties<Mqtt5ConnectImpl> implements Mqtt5Connect {

    private final Mqtt5ClientIdentifierImpl clientIdentifier;
    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final RestrictionsImpl restrictions;
    private final SimpleAuthImpl simpleAuth;
    private final Mqtt5ExtendedAuthImpl extendedAuth;
    private final Mqtt5WillPublishImpl willPublish;

    public Mqtt5ConnectImpl(
            @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier, final int keepAlive, final boolean isCleanStart,
            final long sessionExpiryInterval, final boolean isResponseInformationRequested,
            final boolean isProblemInformationRequested, @NotNull final RestrictionsImpl restrictions,
            @Nullable final SimpleAuthImpl simpleAuth, @Nullable final Mqtt5ExtendedAuthImpl extendedAuth,
            @Nullable final Mqtt5WillPublishImpl willPublish, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5ConnectImpl, ? extends Mqtt5MessageEncoder<Mqtt5ConnectImpl>> encoderProvider) {

        super(userProperties, encoderProvider);
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
    }

    @NotNull
    @Override
    public Optional<Mqtt5ClientIdentifier> getClientIdentifier() {
        return clientIdentifier == Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER ? Optional.empty() :
                Optional.of(clientIdentifier);
    }

    @NotNull
    public Mqtt5ClientIdentifierImpl getRawClientIdentifier() {
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
    public RestrictionsImpl getRestrictions() {
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
    public Optional<Mqtt5WillPublish> getWillPublish() {
        return Optional.ofNullable(willPublish);
    }

    @Nullable
    public Mqtt5WillPublishImpl getRawWillPublish() {
        return willPublish;
    }

    @Override
    protected Mqtt5ConnectImpl getCodable() {
        return this;
    }


    public static class SimpleAuthImpl implements SimpleAuth {

        private final Mqtt5UTF8StringImpl username;
        private final byte[] password;

        public SimpleAuthImpl(@Nullable final Mqtt5UTF8StringImpl username, @Nullable final byte[] password) {
            this.username = username;
            this.password = password;
        }

        @NotNull
        @Override
        public Optional<Mqtt5UTF8String> getUsername() {
            return Optional.ofNullable(username);
        }

        @Nullable
        public Mqtt5UTF8StringImpl getRawUsername() {
            return username;
        }

        @NotNull
        @Override
        public Optional<ByteBuf> getPassword() {
            return ByteBufUtil.optionalReadOnly(password);
        }

        @Nullable
        public byte[] getRawPassword() {
            return password;
        }

    }


    public static class RestrictionsImpl implements Restrictions {

        @NotNull
        public static final RestrictionsImpl DEFAULT =
                new RestrictionsImpl(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM,
                        DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);

        private final int receiveMaximum;
        private final int topicAliasMaximum;
        private final int maximumPacketSize;

        public RestrictionsImpl(
                final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize) {
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
        public int getMaximumPacketSize() {
            return maximumPacketSize;
        }

    }

}
