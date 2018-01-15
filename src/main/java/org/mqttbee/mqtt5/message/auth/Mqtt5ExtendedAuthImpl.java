package org.mqttbee.mqtt5.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ExtendedAuth;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ExtendedAuthImpl implements Mqtt5ExtendedAuth {

    private final Mqtt5UTF8String method;
    private final byte[] data;

    public Mqtt5ExtendedAuthImpl(@NotNull final Mqtt5UTF8String method, @Nullable final byte[] data) {
        this.method = method;
        this.data = data;
    }

    @NotNull
    @Override
    public Mqtt5UTF8String getMethod() {
        return method;
    }

    @NotNull
    @Override
    public Optional<byte[]> getData() {
        return Optional.ofNullable(data);
    }

    @Nullable
    public byte[] getRawData() {
        return data;
    }

}
