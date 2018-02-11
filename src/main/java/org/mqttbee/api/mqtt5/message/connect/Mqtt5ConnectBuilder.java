package org.mqttbee.api.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5ExtendedAuth;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5ConnectEncoder;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.nio.ByteBuffer;

import static org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.RestrictionsImpl;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.SimpleAuthImpl;

public class Mqtt5ConnectBuilder {

    private Mqtt5ClientIdentifierImpl clientIdentifier; // TODO
    private int keepAlive = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryInterval = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private RestrictionsImpl restrictions;
    private SimpleAuthImpl simpleAuth;
    private Mqtt5ExtendedAuthImpl extendedAuth;
    private Mqtt5WillPublishImpl willPublish;
    private Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5ConnectBuilder() {
    }

    Mqtt5ConnectBuilder(@NotNull final Mqtt5Connect connect) {
        final Mqtt5ConnectImpl connectImpl =
                MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt5ConnectImpl.class);
        clientIdentifier = connectImpl.getRawClientIdentifier();
        keepAlive = connectImpl.getKeepAlive();
        isCleanStart = connectImpl.isCleanStart();
        sessionExpiryInterval = connectImpl.getSessionExpiryInterval();
        isResponseInformationRequested = connectImpl.isResponseInformationRequested();
        isProblemInformationRequested = connectImpl.isProblemInformationRequested();
        restrictions = connectImpl.getRestrictions();
        simpleAuth = connectImpl.getRawSimpleAuth();
        extendedAuth = connectImpl.getRawExtendedAuth();
        willPublish = connectImpl.getRawWillPublish();
        userProperties = connectImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5ConnectBuilder withClientIdentifier(@NotNull final Mqtt5ClientIdentifier clientIdentifier) {
        this.clientIdentifier =
                MustNotBeImplementedUtil.checkNotImplemented(clientIdentifier, Mqtt5ClientIdentifierImpl.class);
        return this;
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
    public Mqtt5ConnectBuilder withExtendedAuth(@Nullable final Mqtt5ExtendedAuth extendedAuth) {
        this.extendedAuth =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(extendedAuth, Mqtt5ExtendedAuthImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withWillPublish(@Nullable final Mqtt5WillPublish willPublish) {
        this.willPublish = MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, Mqtt5WillPublishImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Connect build() {
        return new Mqtt5ConnectImpl(clientIdentifier, keepAlive, isCleanStart, sessionExpiryInterval,
                isResponseInformationRequested, isProblemInformationRequested, restrictions, simpleAuth, extendedAuth,
                willPublish, userProperties, Mqtt5ConnectEncoder.PROVIDER);
    }


    public static class SimpleAuthBuilder {

        private Mqtt5UTF8StringImpl username;
        private ByteBuffer password;

        SimpleAuthBuilder() {
        }

        @NotNull
        public SimpleAuthBuilder withUsername(@Nullable final Mqtt5UTF8String username) {
            this.username = MustNotBeImplementedUtil.checkNullOrNotImplemented(username, Mqtt5UTF8StringImpl.class);
            return this;
        }

        @NotNull
        public SimpleAuthBuilder withPassword(@Nullable final byte[] password) {
            Preconditions.checkArgument((password == null) || Mqtt5DataTypes.isInBinaryDataRange(password));
            this.password = ByteBufferUtil.wrap(password);
            return this;
        }

        @NotNull
        public SimpleAuthBuilder withPassword(@Nullable final ByteBuffer password) {
            Preconditions.checkArgument((password == null) || Mqtt5DataTypes.isInBinaryDataRange(password));
            this.password = ByteBufferUtil.slice(password);
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