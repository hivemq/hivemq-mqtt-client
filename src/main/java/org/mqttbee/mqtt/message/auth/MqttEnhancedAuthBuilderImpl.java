package org.mqttbee.mqtt.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttEnhancedAuthBuilderImpl implements Mqtt5EnhancedAuthBuilder {

    private final MqttUTF8StringImpl method;
    private ByteBuffer data;

    public MqttEnhancedAuthBuilderImpl(@NotNull final MqttUTF8StringImpl method) {
        Preconditions.checkNotNull(method);
        this.method = method;
    }

    @NotNull
    @Override
    public MqttEnhancedAuthBuilderImpl withData(@Nullable final byte[] data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttEnhancedAuthBuilderImpl withData(@Nullable final ByteBuffer data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    public MqttEnhancedAuthImpl build() {
        return new MqttEnhancedAuthImpl(method, data);
    }

}
