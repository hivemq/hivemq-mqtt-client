package org.mqttbee.mqtt.message.connect;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuthImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnectImpl
        extends MqttWrappedMessage<MqttConnectImpl, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>>
        implements Mqtt5Connect {

    private final int keepAlive;
    private final boolean isCleanStart;
    private final long sessionExpiryInterval;
    private final boolean isResponseInformationRequested;
    private final boolean isProblemInformationRequested;
    private final MqttConnectRestrictionsImpl restrictions;
    private final MqttSimpleAuthImpl simpleAuth;
    private final Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private final MqttWillPublishImpl willPublish;

    public MqttConnectImpl(
            final int keepAlive, final boolean isCleanStart, final long sessionExpiryInterval,
            final boolean isResponseInformationRequested, final boolean isProblemInformationRequested,
            @NotNull final MqttConnectRestrictionsImpl restrictions, @Nullable final MqttSimpleAuthImpl simpleAuth,
            @Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider,
            @Nullable final MqttWillPublishImpl willPublish, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttWrappedMessageEncoderProvider<MqttConnectImpl, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>> encoderProvider) {

        super(userProperties, encoderProvider);
        this.keepAlive = keepAlive;
        this.isCleanStart = isCleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.isResponseInformationRequested = isResponseInformationRequested;
        this.isProblemInformationRequested = isProblemInformationRequested;
        this.restrictions = restrictions;
        this.simpleAuth = simpleAuth;
        this.enhancedAuthProvider = enhancedAuthProvider;
        this.willPublish = willPublish;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public boolean isCleanStart() {
        return isCleanStart;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return isResponseInformationRequested;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return isProblemInformationRequested;
    }

    @NotNull
    @Override
    public MqttConnectRestrictionsImpl getRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<Mqtt5SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(simpleAuth);
    }

    @Nullable
    public MqttSimpleAuthImpl getRawSimpleAuth() {
        return simpleAuth;
    }

    @NotNull
    @Override
    public Optional<Mqtt5EnhancedAuthProvider> getEnhancedAuthProvider() {
        return Optional.ofNullable(enhancedAuthProvider);
    }

    @Nullable
    public Mqtt5EnhancedAuthProvider getRawEnhancedAuthProvider() {
        return enhancedAuthProvider;
    }

    @NotNull
    @Override
    public Optional<Mqtt5WillPublish> getWillPublish() {
        return Optional.ofNullable(willPublish);
    }

    @Nullable
    public MqttWillPublishImpl getRawWillPublish() {
        return willPublish;
    }

    @NotNull
    @Override
    protected MqttConnectImpl getCodable() {
        return this;
    }

    public MqttConnectWrapper wrap(
            @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuthImpl enhancedAuth) {

        return new MqttConnectWrapper(this, clientIdentifier, enhancedAuth);
    }

}
