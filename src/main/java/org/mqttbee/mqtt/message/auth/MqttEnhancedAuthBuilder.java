package org.mqttbee.mqtt.message.auth;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttEnhancedAuthBuilder implements Mqtt5EnhancedAuthBuilder {

    private final MqttUTF8StringImpl method;
    private ByteBuffer data;

    public MqttEnhancedAuthBuilder(@NotNull final MqttUTF8StringImpl method) {
        Preconditions.checkNotNull(method);
        this.method = method;
    }

    @NotNull
    @Override
    public MqttEnhancedAuthBuilder withData(@Nullable final byte[] data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    @Override
    public MqttEnhancedAuthBuilder withData(@Nullable final ByteBuffer data) {
        this.data = MqttBuilderUtil.binaryDataOrNull(data);
        return this;
    }

    @NotNull
    public MqttEnhancedAuth build() {
        return new MqttEnhancedAuth(method, data);
    }

}
