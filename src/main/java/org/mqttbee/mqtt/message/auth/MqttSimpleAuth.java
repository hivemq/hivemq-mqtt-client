package org.mqttbee.mqtt.message.auth;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSimpleAuth implements Mqtt5SimpleAuth {

    private final MqttUTF8StringImpl username;
    private final ByteBuffer password;

    public MqttSimpleAuth(@Nullable final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {
        this.username = username;
        this.password = password;
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getUsername() {
        return Optional.ofNullable(username);
    }

    @Nullable
    public MqttUTF8StringImpl getRawUsername() {
        return username;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPassword() {
        return ByteBufferUtil.optionalReadOnly(password);
    }

    @Nullable
    public ByteBuffer getRawPassword() {
        return password;
    }

}
