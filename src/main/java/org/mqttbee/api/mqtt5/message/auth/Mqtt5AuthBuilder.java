package org.mqttbee.api.mqtt5.message.auth;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5AuthBuilder {

    @NotNull
    Mqtt5AuthBuilder withData(@Nullable byte[] data);

    @NotNull
    Mqtt5AuthBuilder withData(@Nullable ByteBuffer data);

    @NotNull
    Mqtt5AuthBuilder withReasonString(@Nullable String reasonString);

    @NotNull
    Mqtt5AuthBuilder withReasonString(@Nullable Mqtt5UTF8String reasonString);

    @NotNull
    Mqtt5AuthBuilder withUserProperties(@NotNull Mqtt5UserProperties userProperties);

}
