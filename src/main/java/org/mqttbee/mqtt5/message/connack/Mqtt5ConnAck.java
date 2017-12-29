package org.mqttbee.mqtt5.message.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnAck implements Mqtt5Message {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final int KEEP_ALIVE_FROM_CONNECT = -1;
    public static final String CLIENT_IDENTIFIER_FROM_CONNECT = null;

    private final Mqtt5ConnAckReasonCode reasonCode;
    private final String reasonString;
    private final boolean isSessionPresent;
    private final long sessionExpiryInterval;
    private final int serverKeepAlive;
    private final String assignedClientIdentifier;
    private final Auth auth;
    private final Restrictions restrictions;
    private final String responseInformation;
    private final String serverReference;
    private final List<Mqtt5UserProperty> userProperties;

    public Mqtt5ConnAck(
            @NotNull final Mqtt5ConnAckReasonCode reasonCode, @Nullable final String reasonString,
            final boolean isSessionPresent, final long sessionExpiryInterval, final int serverKeepAlive,
            @Nullable final String assignedClientIdentifier, @Nullable final Auth auth,
            @NotNull final Restrictions restrictions, @Nullable final String responseInformation,
            @Nullable final String serverReference, @Nullable final List<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.reasonString = reasonString;
        this.isSessionPresent = isSessionPresent;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverKeepAlive = serverKeepAlive;
        this.assignedClientIdentifier = assignedClientIdentifier;
        this.auth = auth;
        this.restrictions = restrictions;
        this.responseInformation = responseInformation;
        this.serverReference = serverReference;
        this.userProperties = userProperties;
    }

    @NotNull
    public Mqtt5ConnAckReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    public Optional<String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    public boolean isSessionPresent() {
        return isSessionPresent;
    }

    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public int getServerKeepAlive() {
        return serverKeepAlive;
    }

    @NotNull
    public Optional<String> getAssignedClientIdentifier() {
        return Optional.ofNullable(assignedClientIdentifier);
    }

    @NotNull
    public Optional<Auth> getAuth() {
        return Optional.ofNullable(auth);
    }

    @NotNull
    public Restrictions getRestrictions() {
        return restrictions;
    }

    @NotNull
    public Optional<String> getResponseInformation() {
        return Optional.ofNullable(responseInformation);
    }

    @NotNull
    public Optional<String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    @NotNull
    public Optional<List<Mqtt5UserProperty>> getUserProperties() {
        return Optional.ofNullable(userProperties);
    }


    public static class Auth {

        public static final Auth DEFAULT = null;

        private final String method;
        private final byte[] data;

        public Auth(@Nullable final String method, @Nullable final byte[] data) {
            this.method = method;
            this.data = data;
        }

        @NotNull
        public Optional<String> getMethod() {
            return Optional.ofNullable(method);
        }

        @NotNull
        public Optional<byte[]> getData() {
            return Optional.ofNullable(data);
        }

    }

    public static class Restrictions {

        public static final int DEFAULT_RECEIVE_MAXIMUM = 65_535;
        public static final int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
        public static final long DEFAULT_MAXIMUM_PACKET_SIZE_INFINITY = -1;
        public static final byte DEFAULT_MAXIMUM_QOS = 2;
        public static final boolean DEFAULT_RETAIN_AVAILABLE = true;
        public static final boolean DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE = true;
        public static final boolean DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE = true;
        public static final boolean DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE = true;
        public static final Restrictions DEFAULT = new Restrictions(
                DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_INFINITY,
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

        public Restrictions(
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

        public int getReceiveMaximum() {
            return receiveMaximum;
        }

        public int getTopicAliasMaximum() {
            return topicAliasMaximum;
        }

        public long getMaximumPacketSize() {
            return maximumPacketSize;
        }

        public byte getMaximumQoS() {
            return maximumQoS;
        }

        public boolean isRetainAvailable() {
            return isRetainAvailable;
        }

        public boolean isWildcardSubscriptionAvailable() {
            return isWildcardSubscriptionAvailable;
        }

        public boolean isSubscriptionIdentifierAvailable() {
            return isSubscriptionIdentifierAvailable;
        }

        public boolean isSharedSubscriptionAvailable() {
            return isSharedSubscriptionAvailable;
        }

    }

}
