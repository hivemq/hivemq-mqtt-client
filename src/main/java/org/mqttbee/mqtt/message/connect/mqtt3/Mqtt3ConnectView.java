package org.mqttbee.mqtt.message.connect.mqtt3;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3ConnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3ConnectView implements Mqtt3Connect {

    @NotNull
    public static MqttConnectImpl wrapped(
            final int keepAlive, final boolean isCleanSession,
            @Nullable final MqttConnectImpl.SimpleAuthImpl simpleAuth,
            @Nullable final MqttWillPublishImpl willPublish) {

        return new MqttConnectImpl(keepAlive, isCleanSession, isCleanSession ? 0 : MqttConnectImpl.NO_SESSION_EXPIRY,
                MqttConnectImpl.DEFAULT_RESPONSE_INFORMATION_REQUESTED,
                MqttConnectImpl.DEFAULT_PROBLEM_INFORMATION_REQUESTED, MqttConnectImpl.RestrictionsImpl.DEFAULT,
                simpleAuth, null, willPublish, MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3ConnectEncoder.PROVIDER);
    }

    @NotNull
    public static Mqtt3ConnectView create(
            final int keepAlive, final boolean isCleanSession,
            @Nullable final MqttConnectImpl.SimpleAuthImpl simpleAuth,
            @Nullable final MqttWillPublishImpl willPublish) {

        return new Mqtt3ConnectView(wrapped(keepAlive, isCleanSession, simpleAuth, willPublish));
    }

    private final MqttConnectImpl wrapped;

    private Mqtt3ConnectView(@NotNull final MqttConnectImpl wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getKeepAlive() {
        return wrapped.getKeepAlive();
    }

    @Override
    public boolean isCleanSession() {
        return wrapped.isCleanStart();
    }

    @NotNull
    @Override
    public Optional<SimpleAuth> getSimpleAuth() {
        final MqttConnectImpl.SimpleAuthImpl simpleAuth = wrapped.getRawSimpleAuth();
        return (simpleAuth == null) ? Optional.empty() : Optional.of(new SimpleAuthView(simpleAuth));
    }

    @NotNull
    @Override
    public Optional<Mqtt3Publish> getWillPublish() {
        return Optional.empty();
    }

    @NotNull
    public MqttConnectImpl getWrapped() {
        return wrapped;
    }


    @Immutable
    public class SimpleAuthView implements SimpleAuth {

        private final MqttConnectImpl.SimpleAuthImpl simpleAuth;

        private SimpleAuthView(@NotNull final MqttConnectImpl.SimpleAuthImpl simpleAuth) {
            this.simpleAuth = simpleAuth;
        }

        @NotNull
        @Override
        public MqttUTF8String getUsername() {
            final MqttUTF8StringImpl username = simpleAuth.getRawUsername();
            if (username == null) {
                throw new IllegalStateException();
            }
            return username;
        }

        @NotNull
        @Override
        public Optional<ByteBuffer> getPassword() {
            return simpleAuth.getPassword();
        }

    }

}
