package org.mqttbee.mqtt.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5AuthEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttAuthBuilderImpl implements Mqtt5AuthBuilder {

    private final MqttUTF8StringImpl method;
    private ByteBuffer data;
    private final Mqtt5AuthReasonCode reasonCode;
    private MqttUTF8StringImpl reasonString;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttAuthBuilderImpl(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final MqttUTF8StringImpl method) {

        Preconditions.checkNotNull(reasonCode);
        Preconditions.checkNotNull(method);
        this.reasonCode = reasonCode;
        this.method = method;
    }

    @NotNull
    @Override
    public MqttAuthBuilderImpl withData(@Nullable final byte[] data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilderImpl withData(@Nullable final ByteBuffer data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilderImpl withReasonString(@Nullable final String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilderImpl withReasonString(@Nullable final MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @NotNull
    @Override
    public MqttAuthBuilderImpl withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public MqttAuthImpl build() {
        return new MqttAuthImpl(
                reasonCode, method, data, reasonString, userProperties, Mqtt5AuthEncoder.PROVIDER); // TODO
    }

}
