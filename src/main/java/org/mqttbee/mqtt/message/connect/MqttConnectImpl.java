package org.mqttbee.mqtt.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttConnectImpl
        extends MqttWrappedMessage<MqttConnectImpl, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>>
        implements Mqtt5Connect {

    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final RestrictionsImpl restrictions;
    private final SimpleAuthImpl simpleAuth;
    private final Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private final MqttWillPublishImpl willPublish;

    public MqttConnectImpl(
            final int keepAlive, final boolean isCleanStart, final long sessionExpiryInterval,
            final boolean isResponseInformationRequested, final boolean isProblemInformationRequested,
            @NotNull final RestrictionsImpl restrictions, @Nullable final SimpleAuthImpl simpleAuth,
            @Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider,
            @Nullable final MqttWillPublishImpl willPublish, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttConnectImpl, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>> encoderProvider) {

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
    public MqttWillPublishImpl getRawWillPublish() {
        return willPublish;
    }

    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNECT;
    }

    @NotNull
    @Override
    protected MqttConnectImpl getCodable() {
        return this;
    }

    public MqttConnectWrapper wrap(
            @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuthImpl enhancedAuth) {

        return new MqttConnectWrapper(this, clientIdentifier, enhancedAuth);
    }


    public static class SimpleAuthImpl implements SimpleAuth {

        private final MqttUTF8StringImpl username;
        private final ByteBuffer password;

        public SimpleAuthImpl(@Nullable final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {
            this.username = username;
            this.password = password;
        }

        @NotNull
        @Override
        public Optional<MqttUTF8String> getUsername() {
            return Optional.ofNullable(username);
        }

        @Nullable
        public MqttUTF8StringImpl getRawUsername() {
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
