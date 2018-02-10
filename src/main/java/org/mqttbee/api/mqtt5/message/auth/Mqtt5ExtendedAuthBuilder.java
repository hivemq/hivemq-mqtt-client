package org.mqttbee.api.mqtt5.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ExtendedAuthBuilder {

    private Mqtt5UTF8StringImpl method;
    private byte[] data;

    @NotNull
    public Mqtt5ExtendedAuthBuilder withMethod(@NotNull final Mqtt5UTF8String method) {
        this.method = MustNotBeImplementedUtil.checkNotImplemented(method, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ExtendedAuthBuilder withData(@NotNull final byte[] data) {
        Preconditions.checkNotNull(method);
        Preconditions.checkArgument(Mqtt5DataTypes.isInBinaryDataRange(data));
        this.data = data;
        return this;
    }

    @NotNull
    public Mqtt5ExtendedAuth build() {
        Preconditions.checkNotNull(method);
        return new Mqtt5ExtendedAuthImpl(method, data);
    }

}
