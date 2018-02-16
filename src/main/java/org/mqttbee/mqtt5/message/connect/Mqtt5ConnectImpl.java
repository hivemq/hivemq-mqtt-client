package org.mqttbee.mqtt5.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5WrappedMessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectImpl extends Mqtt5WrappedMessage<Mqtt5ConnectImpl, Mqtt5ConnectWrapper>
        implements Mqtt5Connect {

    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final RestrictionsImpl restrictions;
    private final SimpleAuthImpl simpleAuth;
    private final Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private final Mqtt5WillPublishImpl willPublish;

    public Mqtt5ConnectImpl(
            final int keepAlive, final boolean isCleanStart, final long sessionExpiryInterval,
            final boolean isResponseInformationRequested, final boolean isProblemInformationRequested,
            @NotNull final RestrictionsImpl restrictions, @Nullable final SimpleAuthImpl simpleAuth,
            @Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider,
            @Nullable final Mqtt5WillPublishImpl willPublish, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5ConnectImpl, ? extends Mqtt5WrappedMessageEncoder<Mqtt5ConnectImpl, Mqtt5ConnectWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.keepAlive = keepAlive;
        this.isCleanStart = isCleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.isResponseInformationRequested = isResponseInformationRequested;
        this.isProblemInformationRequested = isProblemInformationRequested;
        this.restrictions = restrictions;
        this.simpleAuth = simpleAuth;
        this.enhancedAuthProvider = enhancedAuthProvider;
        this.willPublish = willPublish;
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
    public Optional<Mqtt5EnhancedAuthProvider> getEnhancedAuthProvider() {
        return Optional.ofNullable(enhancedAuthProvider);
    }

    @Nullable
    public Mqtt5EnhancedAuthProvider getRawEnhancedAuthProvider() {
        return enhancedAuthProvider;
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

    public Mqtt5ConnectWrapper wrap(
            @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier,
            @Nullable final Mqtt5EnhancedAuthImpl enhancedAuth) {

        return new Mqtt5ConnectWrapper(this, clientIdentifier, enhancedAuth);
    }


    public static class SimpleAuthImpl implements SimpleAuth {

        private final Mqtt5UTF8StringImpl username;
        private final ByteBuffer password;

        public SimpleAuthImpl(@Nullable final Mqtt5UTF8StringImpl username, @Nullable final ByteBuffer password) {
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
        public Optional<ByteBuffer> getPassword() {
            return ByteBufferUtil.optionalReadOnly(password);
        }

        @Nullable
        public ByteBuffer getRawPassword() {
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
