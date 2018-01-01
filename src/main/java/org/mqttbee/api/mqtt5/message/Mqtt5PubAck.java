package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5PubAck extends Mqtt5Message {

    @NotNull
    Mqtt5PubAckReasonCode getReasonCode();

    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    @NotNull
    List<Mqtt5UserProperty> getUserProperties();

}
