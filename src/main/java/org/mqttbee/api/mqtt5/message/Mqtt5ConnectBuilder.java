package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt5.message.Mqtt5Connect.*;
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

    public Mqtt5ConnectBuilder setClientIdentifier(@NotNull final Mqtt5ClientIdentifier clientIdentifier) {
        Preconditions.checkNotNull(clientIdentifier);
        this.clientIdentifier =
                MustNotBeImplementedUtil.checkNotImplemented(clientIdentifier, Mqtt5ClientIdentifierImpl.class);
        return this;
    }

    public Mqtt5ConnectBuilder setKeepAlive(final int keepAlive) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAlive));
        this.keepAlive = keepAlive;
        return this;
    }

    public Mqtt5ConnectBuilder setCleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    public Mqtt5ConnectBuilder setSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    public Mqtt5ConnectBuilder setResponseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    public Mqtt5ConnectBuilder setProblemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    public Mqtt5ConnectBuilder setRestrictions(@NotNull final Mqtt5Connect.Restrictions restrictions) {
        Preconditions.checkNotNull(restrictions);
        this.restrictions = MustNotBeImplementedUtil.checkNotImplemented(restrictions, RestrictionsImpl.class);
        return this;
    }

    public Mqtt5ConnectBuilder setSimpleAuth(@NotNull final Mqtt5Connect.SimpleAuth simpleAuth) {
        Preconditions.checkNotNull(simpleAuth);
        this.simpleAuth = MustNotBeImplementedUtil.checkNotImplemented(simpleAuth, SimpleAuthImpl.class);
        return this;
    }

    public Mqtt5ConnectBuilder setExtendedAuth(@NotNull final Mqtt5ExtendedAuth extendedAuth) {
        Preconditions.checkNotNull(extendedAuth);
        this.extendedAuth = MustNotBeImplementedUtil.checkNotImplemented(extendedAuth, Mqtt5ExtendedAuthImpl.class);
        return this;
    }

    public Mqtt5ConnectBuilder setWillPublish(@NotNull final Mqtt5WillPublish willPublish) {
        Preconditions.checkNotNull(willPublish);
        this.willPublish = MustNotBeImplementedUtil.checkNotImplemented(willPublish, Mqtt5WillPublishImpl.class);
        return this;
    }

    public Mqtt5ConnectBuilder setUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        Preconditions.checkNotNull(userProperties);
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    public Mqtt5Connect build() {
        return new Mqtt5ConnectImpl(clientIdentifier, keepAlive, isCleanStart, sessionExpiryInterval,
                isResponseInformationRequested, isProblemInformationRequested, restrictions, simpleAuth, extendedAuth,
                willPublish, userProperties);
    }


    public static class SimpleAuthBuilder {

        private Mqtt5UTF8StringImpl username;
        private byte[] password;

        public SimpleAuthBuilder setUsername(@NotNull final Mqtt5UTF8String username) {
            Preconditions.checkNotNull(username);
            this.username = MustNotBeImplementedUtil.checkNotImplemented(username, Mqtt5UTF8StringImpl.class);
            return this;
        }

        public SimpleAuthBuilder setPassword(@NotNull final byte[] password) {
            Preconditions.checkNotNull(password);
            Preconditions.checkArgument(Mqtt5DataTypes.isInBinaryDataRange(password));
            this.password = password;
            return this;
        }

        public SimpleAuth build() {
            Preconditions.checkState(username != null || password != null);
            return new SimpleAuthImpl(username, password);
        }

    }


    public static class RestrictionsBuilder {

        private int receiveMaximum = Restrictions.DEFAULT_RECEIVE_MAXIMUM;
        private int topicAliasMaximum = Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
        private int maximumPacketSize = Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;

        public RestrictionsBuilder setReceiveMaximum(final int receiveMaximum) {
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(receiveMaximum));
            this.receiveMaximum = receiveMaximum;
            return this;
        }

        public RestrictionsBuilder setTopicAliasMaximum(final int topicAliasMaximum) {
            Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(topicAliasMaximum));
            this.topicAliasMaximum = topicAliasMaximum;
            return this;
        }

        public RestrictionsBuilder setMaximumPacketSize(final int maximumPacketSize) {
            Preconditions.checkArgument(Mqtt5DataTypes.isInVariableByteIntegerRange(maximumPacketSize));
            this.maximumPacketSize = maximumPacketSize;
            return this;
        }

        public Restrictions build() {
            return new RestrictionsImpl(receiveMaximum, topicAliasMaximum, maximumPacketSize);
        }

    }

}