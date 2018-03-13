package org.mqttbee.mqtt;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientConnectionData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
public class MqttClientDataImpl implements Mqtt5ClientData {

    private static final AttributeKey<MqttClientDataImpl> KEY = AttributeKey.valueOf("client.data");

    @NotNull
    public static MqttClientDataImpl from(@NotNull final Channel channel) {
        return Preconditions.checkNotNull(channel.attr(KEY).get());
    }

    private final MqttVersion mqttVersion;
    private MqttClientIdentifierImpl clientIdentifier;
    private final String serverHost;
    private final int serverPort;
    private final boolean usesSSL;
    private final AtomicBoolean connecting;
    private final AtomicBoolean connected;
    private MqttClientConnectionDataImpl clientConnectionData;
    private MqttServerConnectionDataImpl serverConnectionData;
    private final boolean followsRedirects;
    private final boolean allowsServerReAuth;
    private final MqttClientExecutorConfigImpl executorConfig;

    public MqttClientDataImpl(
            @NotNull final MqttVersion mqttVersion, @Nullable final MqttClientIdentifierImpl clientIdentifier,
            @NotNull final String serverHost, final int serverPort, final boolean usesSSL,
            final boolean followsRedirects, final boolean allowsServerReAuth,
            @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        this.mqttVersion = mqttVersion;
        this.clientIdentifier = clientIdentifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.usesSSL = usesSSL;
        this.connecting = new AtomicBoolean();
        this.connected = new AtomicBoolean();
        this.followsRedirects = followsRedirects;
        this.allowsServerReAuth = allowsServerReAuth;
        this.executorConfig = executorConfig;
    }

    @NotNull
    public MqttVersion getMqttVersion() {
        return mqttVersion;
    }

    @NotNull
    @Override
    public Optional<MqttClientIdentifier> getClientIdentifier() {
        return (clientIdentifier == MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) ? Optional.empty() :
                Optional.of(clientIdentifier);
    }

    @NotNull
    public MqttClientIdentifierImpl getRawClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(@NotNull final MqttClientIdentifierImpl clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @NotNull
    @Override
    public String getServerHost() {
        return serverHost;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public boolean usesSSL() {
        return usesSSL;
    }

    @Override
    public boolean isConnecting() {
        return connecting.get();
    }

    public boolean setConnecting(final boolean connecting) {
        return this.connecting.compareAndSet(!connecting, connecting);
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    public boolean setConnected(final boolean connected) {
        return this.connected.compareAndSet(!connected, connected);
    }

    @NotNull
    @Override
    public Optional<Mqtt5ClientConnectionData> getClientConnectionData() {
        return Optional.of(clientConnectionData);
    }

    @Nullable
    public MqttClientConnectionDataImpl getRawClientConnectionData() {
        return clientConnectionData;
    }

    public void setClientConnectionData(@Nullable final MqttClientConnectionDataImpl clientConnectionData) {
        this.clientConnectionData = clientConnectionData;
    }

    @NotNull
    @Override
    public Optional<Mqtt5ServerConnectionData> getServerConnectionData() {
        return Optional.of(serverConnectionData);
    }

    @Nullable
    public MqttServerConnectionDataImpl getRawServerConnectionData() {
        return serverConnectionData;
    }

    public void setServerConnectionData(@Nullable final MqttServerConnectionDataImpl serverConnectionData) {
        this.serverConnectionData = serverConnectionData;
    }

    @Override
    public boolean followsRedirects() {
        return followsRedirects;
    }

    @Override
    public boolean allowsServerReAuth() {
        return allowsServerReAuth;
    }

    @NotNull
    @Override
    public MqttClientExecutorConfigImpl getExecutorConfig() {
        return executorConfig;
    }

    public void to(@NotNull final Channel channel) {
        channel.attr(KEY).set(this);
    }

}
