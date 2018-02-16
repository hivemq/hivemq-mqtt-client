package org.mqttbee.mqtt5;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.Mqtt5ClientConnectionData;
import org.mqttbee.api.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientDataImpl implements Mqtt5ClientData {

    private static final AttributeKey<Mqtt5ClientDataImpl> KEY = AttributeKey.valueOf("client.data");

    @NotNull
    public static Mqtt5ClientDataImpl from(@NotNull final Channel channel) {
        return Preconditions.checkNotNull(channel.attr(KEY).get());
    }

    private Mqtt5ClientIdentifierImpl clientIdentifier;
    private final String serverHost;
    private final int serverPort;
    private final boolean usesSSL;
    private final AtomicBoolean connecting;
    private final AtomicBoolean connected;
    private Mqtt5ClientConnectionDataImpl clientConnectionData;
    private Mqtt5ServerConnectionDataImpl serverConnectionData;
    private final boolean followsRedirects;
    private final boolean allowsServerReAuth;
    private final Executor executor;
    private final int numberOfNettyThreads;

    public Mqtt5ClientDataImpl(
            @NotNull final Mqtt5ClientIdentifierImpl clientIdentifier, @NotNull final String serverHost,
            final int serverPort, final boolean usesSSL, final boolean followsRedirects,
            final boolean allowsServerReAuth, @Nullable final Executor executor, final int numberOfNettyThreads) {

        this.clientIdentifier = clientIdentifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.usesSSL = usesSSL;
        this.connecting = new AtomicBoolean();
        this.connected = new AtomicBoolean();
        this.followsRedirects = followsRedirects;
        this.allowsServerReAuth = allowsServerReAuth;
        this.executor = executor;
        this.numberOfNettyThreads = numberOfNettyThreads;
    }

    @NotNull
    @Override
    public Optional<Mqtt5ClientIdentifier> getClientIdentifier() {
        return (clientIdentifier == Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) ?
                Optional.empty() : Optional.of(clientIdentifier);
    }

    @NotNull
    public Mqtt5ClientIdentifier getRawClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(@NotNull final Mqtt5ClientIdentifierImpl clientIdentifier) {
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

    @NotNull
    public Mqtt5ClientConnectionDataImpl getRawClientConnectionData() {
        return Preconditions.checkNotNull(clientConnectionData);
    }

    public void setClientConnectionData(@NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData) {
        this.clientConnectionData = clientConnectionData;
    }

    @NotNull
    @Override
    public Optional<Mqtt5ServerConnectionData> getServerConnectionData() {
        return Optional.of(serverConnectionData);
    }

    @Nullable
    public Mqtt5ServerConnectionDataImpl getRawServerConnectionData() {
        return serverConnectionData;
    }

    public void setServerConnectionData(@NotNull final Mqtt5ServerConnectionDataImpl serverConnectionData) {
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

    @Nullable
    public Executor getExecutor() {
        return executor;
    }

    public int getNumberOfNettyThreads() {
        return numberOfNettyThreads;
    }

    public void to(@NotNull final Channel channel) {
        channel.attr(KEY).set(this);
    }

}
