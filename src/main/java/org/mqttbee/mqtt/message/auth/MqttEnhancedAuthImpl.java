package org.mqttbee.mqtt.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttEnhancedAuthImpl implements Mqtt5EnhancedAuth {

    private final MqttUTF8StringImpl method;
    private final ByteBuffer data;

    public MqttEnhancedAuthImpl(@NotNull final MqttUTF8StringImpl method, @Nullable final ByteBuffer data) {
        this.method = method;
        this.data = data;
    }

    @NotNull
    @Override
    public MqttUTF8StringImpl getMethod() {
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
