package org.mqttbee.api.mqtt.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ClientConnectionData {

    int getKeepAlive();

    long getSessionExpiryInterval();

    int getReceiveMaximum();

    int getTopicAliasMaximum();

    int getMaximumPacketSize();

    @NotNull
    Optional<MqttUTF8String> getAuthMethod();

    boolean hasWillPublish();

    boolean isProblemInformationRequested();

    boolean isResponseInformationRequested();

}
