package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;

import java.util.Optional;

/**
 * Data for extended authentication and/or authorization.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5ExtendedAuth {

    /**
     * @return the authentication/authorization method.
     */
    @NotNull
    Mqtt5UTF8String getMethod();

    /**
     * @return the optional authentication/authorization data.
     */
    @NotNull
    Optional<byte[]> getData();

}
