package org.mqttbee.mqtt5.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt5.Mqtt5BuilderUtil;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5AuthEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthBuilderImpl implements Mqtt5AuthBuilder {

    private final Mqtt5UTF8StringImpl method;
    private ByteBuffer data;
    private final Mqtt5AuthReasonCode reasonCode;
    private Mqtt5UTF8StringImpl reasonString;
    private Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5AuthBuilderImpl(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final Mqtt5UTF8StringImpl method) {

        Preconditions.checkNotNull(reasonCode);
        Preconditions.checkNotNull(method);
        this.reasonCode = reasonCode;
        this.method = method;
    }

    @NotNull
    @Override
    public Mqtt5AuthBuilderImpl withData(@Nullable final byte[] data) {
        this.data = Mqtt5BuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5AuthBuilderImpl withData(@Nullable final ByteBuffer data) {
        this.data = Mqtt5BuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5AuthBuilderImpl withReasonString(@Nullable final String reasonString) {
        this.reasonString = Mqtt5BuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5AuthBuilderImpl withReasonString(@Nullable final Mqtt5UTF8String reasonString) {
        this.reasonString = Mqtt5BuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5AuthBuilderImpl withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5AuthImpl build() {
        return new Mqtt5AuthImpl(reasonCode, method, data, reasonString, userProperties, Mqtt5AuthEncoder.PROVIDER);
    }

}
