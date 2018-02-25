package org.mqttbee.api.mqtt.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Simple authentication and/or authorization related data in the MQTT 5 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt5SimpleAuth {

    @NotNull
    static Mqtt5SimpleAuthBuilder builder() {
        return new Mqtt5SimpleAuthBuilder();
    }

    /**
     * @return the optional username.
     */
    @NotNull
    Optional<MqttUTF8String> getUsername();

    /**
     * @return the optional password.
     */
    @NotNull
    Optional<ByteBuffer> getPassword();

}
