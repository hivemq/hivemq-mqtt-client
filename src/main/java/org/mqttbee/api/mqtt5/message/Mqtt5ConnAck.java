package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ConnAck {

    @NotNull
    Mqtt5ConnAckReasonCode getReasonCode();

    boolean isSessionPresent();

    @NotNull
    Optional<Long> getSessionExpiryInterval();

    @NotNull
    Optional<Integer> getServerKeepAlive();

    @NotNull
    Optional<Mqtt5ClientIdentifier> getAssignedClientIdentifier();

    @NotNull
    Optional<Auth> getAuth();

    @NotNull
    Restrictions getRestrictions();

    @NotNull
    Optional<Mqtt5UTF8String> getResponseInformation();

    @NotNull
    Optional<Mqtt5UTF8String> getServerReference();

    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    interface Auth {

        @NotNull
        Optional<Mqtt5UTF8String> getMethod();

        @NotNull
        Optional<byte[]> getData();

    }


    interface Restrictions {

        int DEFAULT_RECEIVE_MAXIMUM = 65_535;
        int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
        long DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = Integer.MAX_VALUE;
        byte DEFAULT_MAXIMUM_QOS = 2;
        boolean DEFAULT_RETAIN_AVAILABLE = true;
        boolean DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE = true;
        boolean DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE = true;
        boolean DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE = true;

        int getReceiveMaximum();

        int getTopicAliasMaximum();

        long getMaximumPacketSize();

        byte getMaximumQoS();

        boolean isRetainAvailable();

        boolean isWildcardSubscriptionAvailable();

        boolean isSubscriptionIdentifierAvailable();

        boolean isSharedSubscriptionAvailable();

    }

}
