package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5SubAck extends Mqtt5Message {

    @NotNull
    ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes();

    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
