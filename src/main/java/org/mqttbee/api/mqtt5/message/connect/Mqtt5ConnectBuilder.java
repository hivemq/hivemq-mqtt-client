package org.mqttbee.api.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5ConnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect.*;
import static org.mqttbee.mqtt.message.connect.MqttConnectImpl.RestrictionsImpl;
import static org.mqttbee.mqtt.message.connect.MqttConnectImpl.SimpleAuthImpl;

public class Mqtt5ConnectBuilder {

    private int keepAlive = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryInterval = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private RestrictionsImpl restrictions;
    private SimpleAuthImpl simpleAuth;
    private Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private MqttWillPublishImpl willPublish;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5ConnectBuilder() {
    }

    Mqtt5ConnectBuilder(@NotNull final Mqtt5Connect connect) {
        final MqttConnectImpl connectImpl =
                MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnectImpl.class);
        keepAlive = connectImpl.getKeepAlive();
        isCleanStart = connectImpl.isCleanStart();
        sessionExpiryInterval = connectImpl.getSessionExpiryInterval();
        isResponseInformationRequested = connectImpl.isResponseInformationRequested();
        isProblemInformationRequested = connectImpl.isProblemInformationRequested();
        restrictions = connectImpl.getRestrictions();
        simpleAuth = connectImpl.getRawSimpleAuth();
        enhancedAuthProvider = connectImpl.getRawEnhancedAuthProvider();
        willPublish = connectImpl.getRawWillPublish();
        userProperties = connectImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5ConnectBuilder withKeepAlive(final int keepAlive) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAlive));
        this.keepAlive = keepAlive;
        return this;
    }

    public Mqtt5ConnectBuilder withCleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withResponseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withProblemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withRestrictions(@NotNull final Restrictions restrictions) {
        this.restrictions = MustNotBeImplementedUtil.checkNotImplemented(restrictions, RestrictionsImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSimpleAuth(@Nullable final SimpleAuth simpleAuth) {
        this.simpleAuth = MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, SimpleAuthImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withEnhancedAuth(@Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {
        this.enhancedAuthProvider = enhancedAuthProvider;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withWillPublish(@Nullable final Mqtt5WillPublish willPublish) {
        this.willPublish = MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, MqttWillPublishImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Connect build() {
        return new MqttConnectImpl(keepAlive, isCleanStart, sessionExpiryInterval, isResponseInformationRequested,
                isProblemInformationRequested, restrictions, simpleAuth, enhancedAuthProvider, willPublish,
                userProperties, Mqtt5ConnectEncoder.PROVIDER);
    }


    public static class SimpleAuthBuilder {

        private MqttUTF8StringImpl username;
        private ByteBuffer password;

        SimpleAuthBuilder() {
        }

        @NotNull
        public SimpleAuthBuilder withUsername(@Nullable final String username) {
            this.username = MqttBuilderUtil.stringOrNull(username);
            return this;
        }

        @NotNull
        public SimpleAuthBuilder withUsername(@Nullable final Mqtt5UTF8String username) {
            this.username = MqttBuilderUtil.stringOrNull(username);
            return this;
        }

        @NotNull
        public SimpleAuthBuilder withPassword(@Nullable final byte[] password) {
            this.password = MqttBuilderUtil.binaryDataOrNull(password);
            return this;
        }

        @NotNull
        public SimpleAuthBuilder withPassword(@Nullable final ByteBuffer password) {
            this.password = MqttBuilderUtil.binaryDataOrNull(password);
            return this;
        }

        @NotNull
        public SimpleAuth build() {
            Preconditions.checkState(username != null || password != null);
            return new SimpleAuthImpl(username, password);
        }

    }


    public static class RestrictionsBuilder {

        private int receiveMaximum = Restrictions.DEFAULT_RECEIVE_MAXIMUM;
        private int topicAliasMaximum = Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
        private int maximumPacketSize = Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;

        RestrictionsBuilder() {
        }

        @NotNull
        public RestrictionsBuilder withReceiveMaximum(final int receiveMaximum) {
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(receiveMaximum));
            this.receiveMaximum = receiveMaximum;
            return this;
        }

        @NotNull
        public RestrictionsBuilder withTopicAliasMaximum(final int topicAliasMaximum) {
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(topicAliasMaximum));
            this.topicAliasMaximum = topicAliasMaximum;
            return this;
        }

        @NotNull
        public RestrictionsBuilder withMaximumPacketSize(final int maximumPacketSize) {
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(maximumPacketSize));
            this.maximumPacketSize = maximumPacketSize;
            return this;
        }

        @NotNull
        public Restrictions build() {
            return new RestrictionsImpl(receiveMaximum, topicAliasMaximum, maximumPacketSize);
        }

    }

}