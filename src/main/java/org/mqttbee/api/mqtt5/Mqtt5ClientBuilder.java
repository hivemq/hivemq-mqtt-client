package org.mqttbee.api.mqtt5;

import dagger.internal.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder {

    private final MqttClientIdentifierImpl identifier;
    private final String serverHost;
    private final int serverPort;
    private final boolean usesSSL;
    private final Executor executor;
    private final int numberOfNettyThreads;

    private boolean followsRedirects = false;
    private boolean allowsServerReAuth = false;

    public Mqtt5ClientBuilder(
            @NotNull final MqttClientIdentifierImpl identifier, @NotNull final String serverHost, final int serverPort,
            final boolean usesSSL, final Executor executor, final int numberOfNettyThreads) {

        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(serverHost);

        this.identifier = identifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.usesSSL = usesSSL;
        this.executor = executor;
        this.numberOfNettyThreads = numberOfNettyThreads;
    }

    @NotNull
    public Mqtt5ClientBuilder followingRedirects(final boolean followsRedirects) {
        this.followsRedirects = followsRedirects;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder allowingServerReAuth(final boolean allowsServerReAuth) {
        this.allowsServerReAuth = allowsServerReAuth;
        return this;
    }

    @NotNull
    public Mqtt5Client reactive() {
        return new Mqtt5ClientImpl(buildClientData());
    }

    private Mqtt5ClientDataImpl buildClientData() {
        return new Mqtt5ClientDataImpl(identifier, serverHost, serverPort, usesSSL, followsRedirects,
                allowsServerReAuth, executor, numberOfNettyThreads);
    }

}
