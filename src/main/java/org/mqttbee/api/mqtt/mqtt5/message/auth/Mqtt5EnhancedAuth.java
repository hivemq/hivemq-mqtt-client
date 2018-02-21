package org.mqttbee.api.mqtt.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Data for enhanced authentication and/or authorization.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5EnhancedAuth {

    /**
     * @return the authentication/authorization method.
     */
    @NotNull
    MqttUTF8String getMethod();

    /**
     * @return the optional authentication/authorization data.
     */
    @NotNull
    Optional<ByteBuffer> getData();

}
