package org.mqttbee.api.mqtt.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5QoS;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ServerConnectionData {

    int getReceiveMaximum();

    int getTopicAliasMaximum();

    int getMaximumPacketSize();

    @NotNull
    Mqtt5QoS getMaximumQoS();

    boolean isRetainAvailable();

    boolean isWildcardSubscriptionAvailable();

    boolean isSubscriptionIdentifierAvailable();

    boolean isSharedSubscriptionAvailable();

}
