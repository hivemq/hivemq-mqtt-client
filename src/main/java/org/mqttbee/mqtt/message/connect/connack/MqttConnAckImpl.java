package org.mqttbee.mqtt.message.connect.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckImpl extends
        MqttMessageWithReasonCode<MqttConnAckImpl, Mqtt5ConnAckReasonCode, MqttMessageEncoderProvider<MqttConnAckImpl>>
        implements Mqtt5ConnAck {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final int KEEP_ALIVE_FROM_CONNECT = -1;
    @Nullable
    public static final MqttClientIdentifierImpl CLIENT_IDENTIFIER_FROM_CONNECT = null;

    private final boolean isSessionPresent;
    private final long sessionExpiryInterval;
    private final int serverKeepAlive;
    private final MqttClientIdentifierImpl assignedClientIdentifier;
    private final Mqtt5EnhancedAuth enhancedAuth;
    private final RestrictionsImpl restrictions;
    private final MqttUTF8StringImpl responseInformation;
    private final MqttUTF8StringImpl serverReference;

    public MqttConnAckImpl(
            @NotNull final Mqtt5ConnAckReasonCode reasonCode, final boolean isSessionPresent,
            final long sessionExpiryInterval, final int serverKeepAlive,
            @Nullable final MqttClientIdentifierImpl assignedClientIdentifier,
            @Nullable final Mqtt5EnhancedAuth enhancedAuth, @NotNull final RestrictionsImpl restrictions,
            @Nullable final MqttUTF8StringImpl responseInformation, @Nullable final MqttUTF8StringImpl serverReference,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

        super(reasonCode, reasonString, userProperties, null);
        this.isSessionPresent = isSessionPresent;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverKeepAlive = serverKeepAlive;
        this.assignedClientIdentifier = assignedClientIdentifier;
        this.enhancedAuth = enhancedAuth;
        this.restrictions = restrictions;
        this.responseInformation = responseInformation;
        this.serverReference = serverReference;
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

    public long getRawSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @NotNull
    @Override
    public Optional<Integer> getServerKeepAlive() {
        return (serverKeepAlive == KEEP_ALIVE_FROM_CONNECT) ? Optional.empty() : Optional.of(serverKeepAlive);
    }

    public int getRawServerKeepAlive() {
        return serverKeepAlive;
    }

    @NotNull
    @Override
    public Optional<MqttClientIdentifier> getAssignedClientIdentifier() {
        return Optional.ofNullable(assignedClientIdentifier);
    }

    @Nullable
    public MqttClientIdentifierImpl getRawAssignedClientIdentifier() {
        return assignedClientIdentifier;
    }

    @NotNull
    @Override
    public Optional<Mqtt5EnhancedAuth> getEnhancedAuth() {
        return Optional.ofNullable(enhancedAuth);
    }

    @Nullable
    public Mqtt5EnhancedAuth getRawEnhancedAuth() {
        return enhancedAuth;
    }

    @NotNull
    @Override
    public RestrictionsImpl getRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getResponseInformation() {
        return Optional.ofNullable(responseInformation);
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    @NotNull
    @Override
    protected MqttConnAckImpl getCodable() {
        return this;
    }


    public static class RestrictionsImpl implements Restrictions {

        @NotNull
        public static final RestrictionsImpl DEFAULT =
                new RestrictionsImpl(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM,
                        DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, DEFAULT_MAXIMUM_QOS, DEFAULT_RETAIN_AVAILABLE,
                        DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE, DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE,
                        DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE);

        private final int receiveMaximum;
        private final int topicAliasMaximum;
        private final int maximumPacketSize;
        private final Mqtt5QoS maximumQoS;
        private final boolean isRetainAvailable;
        private final boolean isWildcardSubscriptionAvailable;
        private final boolean isSubscriptionIdentifierAvailable;
        private final boolean isSharedSubscriptionAvailable;

        public RestrictionsImpl(
                final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize,
                final Mqtt5QoS maximumQoS, final boolean isRetainAvailable,
                final boolean isWildcardSubscriptionAvailable, final boolean isSubscriptionIdentifierAvailable,
                final boolean isSharedSubscriptionAvailable) {
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
        public int getMaximumPacketSize() {
            return maximumPacketSize;
        }

        @Override
        public Mqtt5QoS getMaximumQoS() {
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
