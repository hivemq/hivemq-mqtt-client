package org.mqttbee.mqtt5.message.auth;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ExtendedAuth;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.util.ByteBufUtil;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ExtendedAuthImpl implements Mqtt5ExtendedAuth {

    private final Mqtt5UTF8StringImpl method;
    private final byte[] data;

    public Mqtt5ExtendedAuthImpl(@NotNull final Mqtt5UTF8StringImpl method, @Nullable final byte[] data) {
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
    public Optional<ByteBuf> getData() {
        return ByteBufUtil.optionalReadOnly(data);
    }

    @Nullable
    public byte[] getRawData() {
        return data;
    }

}
