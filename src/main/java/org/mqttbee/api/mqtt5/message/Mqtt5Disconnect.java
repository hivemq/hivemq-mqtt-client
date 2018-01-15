package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import java.util.Optional;

/**
 * MQTT 5 DISCONNECT packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Disconnect {

    /**
     * @return the reason code of this DISCONNECT packet.
     */
    @NotNull
    Mqtt5DisconnectReasonCode getReasonCode();

    /**
     * @return the optional expiry interval for the session, the client disconnects from with this DISCONNECT packet.
     */
    @NotNull
    Optional<Long> getSessionExpiryInterval();

    /**
     * @return the optional server reference, which can be used if the server sent this DISCONNECT packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getServerReference();

    /**
     * @return the optional reason string of this DISCONNECT packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getReasonString();

    /**
     * @return the optional user properties of this DISCONNECT packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
