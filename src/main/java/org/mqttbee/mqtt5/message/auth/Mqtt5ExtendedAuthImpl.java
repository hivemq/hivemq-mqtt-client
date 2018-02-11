package org.mqttbee.mqtt5.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5ExtendedAuth;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ExtendedAuthImpl implements Mqtt5ExtendedAuth {

    private final Mqtt5UTF8StringImpl method;
    private final ByteBuffer data;

    public Mqtt5ExtendedAuthImpl(@NotNull final Mqtt5UTF8StringImpl method, @Nullable final ByteBuffer data) {
        this.method = method;
        this.data = data;
    }

    @NotNull
    @Override
    public Mqtt5UTF8StringImpl getMethod() {
        return method;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getData() {
        return ByteBufferUtil.optionalReadOnly(data);
    }

    @Nullable
    public ByteBuffer getRawData() {
        return data;
    }

}
