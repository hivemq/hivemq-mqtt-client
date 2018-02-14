package org.mqttbee.mqtt5.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5ExtendedAuthBuilder;
import org.mqttbee.mqtt5.Mqtt5BuilderUtil;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ExtendedAuthBuilderImpl implements Mqtt5ExtendedAuthBuilder {

    private final Mqtt5UTF8StringImpl method;
    private ByteBuffer data;

    public Mqtt5ExtendedAuthBuilderImpl(@NotNull final Mqtt5UTF8StringImpl method) {
        Preconditions.checkNotNull(method);
        this.method = method;
    }

    @NotNull
    @Override
    public Mqtt5ExtendedAuthBuilderImpl withData(@Nullable final byte[] data) {
        this.data = Mqtt5BuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ExtendedAuthBuilderImpl withData(@Nullable final ByteBuffer data) {
        this.data = Mqtt5BuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    public Mqtt5ExtendedAuthImpl build() {
        return new Mqtt5ExtendedAuthImpl(method, data);
    }

}
