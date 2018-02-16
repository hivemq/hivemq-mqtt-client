package org.mqttbee.api.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5EnhancedAuthBuilder {

    @NotNull
    Mqtt5EnhancedAuthBuilder withData(@Nullable byte[] data);

    @NotNull
    Mqtt5EnhancedAuthBuilder withData(@Nullable ByteBuffer data);

}
