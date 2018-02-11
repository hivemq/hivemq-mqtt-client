package org.mqttbee.api.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Data for extended authentication and/or authorization.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ExtendedAuth {

    @NotNull
    static Mqtt5ExtendedAuthBuilder builder() {
        return new Mqtt5ExtendedAuthBuilder();
    }

    /**
     * @return the authentication/authorization method.
     */
    @NotNull
    Mqtt5UTF8String getMethod();

    /**
     * @return the optional authentication/authorization data.
     */
    @NotNull
    Optional<ByteBuffer> getData();

}
