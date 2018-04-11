package org.mqttbee.api.mqtt.mqtt3.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Simple authentication and/or authorization related data in the MQTT 3 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt3SimpleAuth {

    @NotNull
    static Mqtt3SimpleAuthBuilder builder() {
        return new Mqtt3SimpleAuthBuilder();
    }

    /**
     * @return the username.
     */
    @NotNull
    MqttUTF8String getUsername();

    /**
     * @return the optional password.
     */
    @NotNull
    Optional<ByteBuffer> getPassword();

}
