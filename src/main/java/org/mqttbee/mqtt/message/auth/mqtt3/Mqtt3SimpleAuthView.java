package org.mqttbee.mqtt.message.auth.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SimpleAuthView implements Mqtt3SimpleAuth {

    @NotNull
    public static MqttSimpleAuth wrapped(
            @NotNull final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {

        return new MqttSimpleAuth(username, password);
    }

    @NotNull
    public static Mqtt3SimpleAuthView create(
            @NotNull final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {

        return new Mqtt3SimpleAuthView(wrapped(username, password));
    }

    private final MqttSimpleAuth wrapped;

    public Mqtt3SimpleAuthView(@NotNull final MqttSimpleAuth wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public MqttUTF8String getUsername() {
        final MqttUTF8StringImpl username = wrapped.getRawUsername();
        if (username == null) {
            throw new IllegalStateException();
        }
        return username;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPassword() {
        return wrapped.getPassword();
    }

    @NotNull
    public MqttSimpleAuth getWrapped() {
        return wrapped;
    }

}
