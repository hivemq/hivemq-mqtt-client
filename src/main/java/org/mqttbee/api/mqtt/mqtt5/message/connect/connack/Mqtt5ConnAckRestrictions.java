package org.mqttbee.api.mqtt.mqtt5.message.connect.connack;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.util.UnsignedDataTypes;

/**
 * Restrictions from the server in the MQTT 5 CONNACK packet.
 */
@DoNotImplement
public interface Mqtt5ConnAckRestrictions {

    /**
     * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the server accepts concurrently.
     */
    int DEFAULT_RECEIVE_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    /**
     * The default maximum amount of topic aliases the server accepts from the client.
     */
    int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
    /**
     * The default maximum packet size the server accepts from the client which indicates that the packet size is
     * not limited beyond the restrictions of the encoding.
     */
    int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
    /**
     * The default maximum QoS the server accepts from the client.
     */
    MqttQoS DEFAULT_MAXIMUM_QOS = MqttQoS.EXACTLY_ONCE;
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
    MqttQoS getMaximumQoS();

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
