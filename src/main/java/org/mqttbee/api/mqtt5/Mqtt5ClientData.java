package org.mqttbee.api.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ClientData {

    @NotNull
    Mqtt5ClientIdentifier getClientIdentifier();

    int getKeepAlive();

    long getSessionExpiryInterval();

    int getReceiveMaximum();

    int getTopicAliasMaximum();

    int getMaximumPacketSize();

    @NotNull
    Optional<Mqtt5UTF8String> getAuthMethod();

    boolean hasWillPublish();

    boolean isProblemInformationRequested();

}
