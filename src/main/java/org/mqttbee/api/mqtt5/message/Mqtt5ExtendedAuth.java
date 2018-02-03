package org.mqttbee.api.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;

import java.util.Optional;

/**
 * Data for extended authentication and/or authorization.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
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
    Optional<ByteBuf> getData();

}
