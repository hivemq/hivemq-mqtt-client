package org.mqttbee.mqtt5.message.connack;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnAckImpl implements Mqtt5ConnAck, Mqtt5Message {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final int KEEP_ALIVE_FROM_CONNECT = -1;
    @Nullable
    public static final Mqtt5ClientIdentifier CLIENT_IDENTIFIER_FROM_CONNECT = null;

    private final Mqtt5ConnAckReasonCode reasonCode;
    private final boolean isSessionPresent;
    private final long sessionExpiryInterval;
    private final int serverKeepAlive;
    private final Mqtt5ClientIdentifier assignedClientIdentifier;
    private final AuthImpl auth;
    private final RestrictionsImpl restrictions;
    private final Mqtt5UTF8String responseInformation;
    private final Mqtt5UTF8String serverReference;
    private final Mqtt5UTF8String reasonString;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5ConnAckImpl(
            @NotNull final Mqtt5ConnAckReasonCode reasonCode, final boolean isSessionPresent,
            final long sessionExpiryInterval, final int serverKeepAlive,
            @Nullable final Mqtt5ClientIdentifier assignedClientIdentifier, @Nullable final AuthImpl auth,
            @NotNull final RestrictionsImpl restrictions, @Nullable final Mqtt5UTF8String responseInformation,
            @Nullable final Mqtt5UTF8String serverReference, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.isSessionPresent = isSessionPresent;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverKeepAlive = serverKeepAlive;
        this.assignedClientIdentifier = assignedClientIdentifier;
        this.auth = auth;
        this.restrictions = restrictions;
        this.responseInformation = responseInformation;
        this.serverReference = serverReference;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5ConnAckReasonCode getReasonCode() {
        return reasonCode;
    }

    @Override
    public boolean isSessionPresent() {
        return isSessionPresent;
    }

    @NotNull
    @Override
    public Optional<Long> getSessionExpiryInterval() {
        return (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT) ? Optional.empty() :
                Optional.of(sessionExpiryInterval);
    }

    @NotNull
    @Override
    public Optional<Integer> getServerKeepAlive() {
        return (serverKeepAlive == KEEP_ALIVE_FROM_CONNECT) ? Optional.empty() : Optional.of(serverKeepAlive);
    }

    @NotNull
    @Override
    public Optional<Mqtt5ClientIdentifier> getAssignedClientIdentifier() {
        return Optional.ofNullable(assignedClientIdentifier);
    }

    @NotNull
    @Override
    public Optional<Auth> getAuth() {
        return Optional.ofNullable(auth);
    }

    @NotNull
    @Override
    public Restrictions getRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getResponseInformation() {
        return Optional.ofNullable(responseInformation);
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int encodedLength() {
        throw new UnsupportedOperationException();
    }


    public static class AuthImpl implements Auth {

        @Nullable
        public static final AuthImpl DEFAULT_NO_AUTH = null;

        private final Mqtt5UTF8String method;
        private final byte[] data;

        public AuthImpl(@Nullable final Mqtt5UTF8String method, @Nullable final byte[] data) {
            this.method = method;
            this.data = data;
        }

        @NotNull
        @Override
        public Optional<Mqtt5UTF8String> getMethod() {
            return Optional.ofNullable(method);
        }

        @NotNull
        @Override
        public Optional<byte[]> getData() {
            return Optional.ofNullable(data);
        }

    }


    public static class RestrictionsImpl implements Restrictions {

        @NotNull
        public static final RestrictionsImpl DEFAULT = new RestrictionsImpl(
                DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT,
                DEFAULT_MAXIMUM_QOS, DEFAULT_RETAIN_AVAILABLE, DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE,
                DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE, DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE);

        private final int receiveMaximum;
        private final int topicAliasMaximum;
        private final long maximumPacketSize;
        private final byte maximumQoS;
        private final boolean isRetainAvailable;
        private final boolean isWildcardSubscriptionAvailable;
        private final boolean isSubscriptionIdentifierAvailable;
        private final boolean isSharedSubscriptionAvailable;

        public RestrictionsImpl(
                final int receiveMaximum, final int topicAliasMaximum, final long maximumPacketSize,
                final byte maximumQoS, final boolean isRetainAvailable, final boolean isWildcardSubscriptionAvailable,
                final boolean isSubscriptionIdentifierAvailable, final boolean isSharedSubscriptionAvailable) {
            this.receiveMaximum = receiveMaximum;
            this.topicAliasMaximum = topicAliasMaximum;
            this.maximumPacketSize = maximumPacketSize;
            this.maximumQoS = maximumQoS;
            this.isRetainAvailable = isRetainAvailable;
            this.isWildcardSubscriptionAvailable = isWildcardSubscriptionAvailable;
            this.isSubscriptionIdentifierAvailable = isSubscriptionIdentifierAvailable;
            this.isSharedSubscriptionAvailable = isSharedSubscriptionAvailable;
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

        @Override
        public byte getMaximumQoS() {
            return maximumQoS;
        }

        @Override
        public boolean isRetainAvailable() {
            return isRetainAvailable;
        }

        @Override
        public boolean isWildcardSubscriptionAvailable() {
            return isWildcardSubscriptionAvailable;
        }

        @Override
        public boolean isSubscriptionIdentifierAvailable() {
            return isSubscriptionIdentifierAvailable;
        }

        @Override
        public boolean isSharedSubscriptionAvailable() {
            return isSharedSubscriptionAvailable;
        }

    }

}
