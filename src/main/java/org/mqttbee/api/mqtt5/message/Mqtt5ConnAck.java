package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import java.util.Optional;

/**
 * MQTT 5 CONNACK packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5ConnAck {

    /**
     * @return the reason code of this CONNACK packet.
     */
    @NotNull
    Mqtt5ConnAckReasonCode getReasonCode();

    /**
     * @return whether the server has a session present.
     */
    boolean isSessionPresent();

    /**
     * @return the optional session expiry interval set from the server. If absent, the session expiry interval from the
     * CONNECT packet is used.
     */
    @NotNull
    Optional<Long> getSessionExpiryInterval();

    /**
     * @return the optional keep alive set from the server. If absent, the keep alive from the CONNECT packet is used.
     */
    @NotNull
    Optional<Integer> getServerKeepAlive();

    /**
     * @return the optional client identifier assigned by the server. If absent, the client identifier from the CONNECT
     * packet is used.
     */
    @NotNull
    Optional<Mqtt5ClientIdentifier> getAssignedClientIdentifier();

    /**
     * @return the optional extended authentication and/or authorization related data of this CONNACK packet.
     */
    @NotNull
    Optional<Mqtt5ExtendedAuth> getExtendedAuth();

    /**
     * @return the restrictions set from the server.
     */
    @NotNull
    Restrictions getRestrictions();

    /**
     * @return the optional response information of this CONNACK packet to retrieve a response topic from.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getResponseInformation();

    /**
     * @return the optional server reference.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getServerReference();

    /**
     * @return the optional reason string of this CONNACK packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this CONNACK packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    /**
     * Restrictions from the the server in the CONNACK packet.
     */
    interface Restrictions {

        /**
         * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the server accepts concurrently.
         */
        int DEFAULT_RECEIVE_MAXIMUM = 65_535;
        /**
         * The default maximum amount of topic aliases the server accepts from the client.
         */
        int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
        /**
         * The default maximum packet size the server accepts from the client which indicates that the packet size is
         * not limited beyond the restrictions of the encoding.
         */
        int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT;
        /**
         * The default maximum QoS the server accepts from the client.
         */
        Mqtt5QoS DEFAULT_MAXIMUM_QOS = Mqtt5QoS.EXACTLY_ONCE;
        /**
         * The default for whether the server accepts retained messages.
         */
        boolean DEFAULT_RETAIN_AVAILABLE = true;
        /**
         * The default for whether the server accepts wildcard subscriptions.
         */
        boolean DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE = true;
        /**
         * The default for whether the server accepts subscription identifiers.
         */
        boolean DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE = true;
        /**
         * The default for whether the server accepts shared subscriptions.
         */
        boolean DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE = true;

        /**
         * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the server accepts concurrently. The
         * default is {@link #DEFAULT_RECEIVE_MAXIMUM}.
         */
        int getReceiveMaximum();

        /**
         * @return the maximum amount of topic aliases the server accepts from the client. The default is {@link
         * #DEFAULT_TOPIC_ALIAS_MAXIMUM}.
         */
        int getTopicAliasMaximum();

        /**
         * @return the maximum packet size the server accepts from the client. The default is {@link
         * #DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT}.
         */
        int getMaximumPacketSize();

        /**
         * @return the maximum QoS the server accepts from the client. The default is {@link #DEFAULT_MAXIMUM_QOS}.
         */
        Mqtt5QoS getMaximumQoS();

        /**
         * @return whether the server accepts retained messages. The default is {@link #DEFAULT_RETAIN_AVAILABLE}.
         */
        boolean isRetainAvailable();

        /**
         * @return whether the server accepts wildcard subscriptions. The default is {@link
         * #DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE}.
         */
        boolean isWildcardSubscriptionAvailable();

        /**
         * @return whether the server accepts subscription identifiers. The default is {@link
         * #DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE}.
         */
        boolean isSubscriptionIdentifierAvailable();

        /**
         * @return whether the server accepts shared subscriptions. The default is {@link
         * #DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE}.
         */
        boolean isSharedSubscriptionAvailable();

    }

}
