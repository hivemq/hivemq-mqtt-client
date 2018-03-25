package org.mqttbee.mqtt.message.connect.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckRestrictions;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnAckRestrictions implements Mqtt5ConnAckRestrictions {

    @NotNull
    public static final MqttConnAckRestrictions DEFAULT =
            new MqttConnAckRestrictions(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM,
                    DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, DEFAULT_MAXIMUM_QOS, DEFAULT_RETAIN_AVAILABLE,
                    DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE, DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE,
                    DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE);

    private final int receiveMaximum;
    private final int topicAliasMaximum;
    private final int maximumPacketSize;
    private final MqttQoS maximumQoS;
    private final boolean isRetainAvailable;
    private final boolean isWildcardSubscriptionAvailable;
    private final boolean isSubscriptionIdentifierAvailable;
    private final boolean isSharedSubscriptionAvailable;

    public MqttConnAckRestrictions(
            final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize,
            final MqttQoS maximumQoS, final boolean isRetainAvailable, final boolean isWildcardSubscriptionAvailable,
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
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public MqttQoS getMaximumQoS() {
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
