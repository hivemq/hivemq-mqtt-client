package org.mqttbee.api.mqtt.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ServerConnectionData {

    int getReceiveMaximum();

    int getTopicAliasMaximum();

    int getMaximumPacketSize();

    @NotNull
    MqttQoS getMaximumQoS();

    boolean isRetainAvailable();

    boolean isWildcardSubscriptionAvailable();

    boolean isSubscriptionIdentifierAvailable();

    boolean isSharedSubscriptionAvailable();

}
