package org.mqttbee.api.mqtt5;

import dagger.internal.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder {

    private final Mqtt5ClientIdentifierImpl identifier;
    private final String host;
    private final int port;
    private final Executor executor;
    private final int numberOfNettyThreads;

    private boolean followRedirects = false;
    private boolean allowServerReAuth = false;

    public Mqtt5ClientBuilder(
            @NotNull final Mqtt5ClientIdentifierImpl identifier, @NotNull final String host, final int port,
            final Executor executor, final int numberOfNettyThreads) {

        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(host);

        this.identifier = identifier;
        this.host = host;
        this.port = port;
        this.executor = executor;
        this.numberOfNettyThreads = numberOfNettyThreads;
    }

    @NotNull
    public Mqtt5ClientBuilder followRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder allowServerReAuth(final boolean allowServerReAuth) {
        this.allowServerReAuth = allowServerReAuth;
        return this;
    }

    @NotNull
    public Mqtt5Client reactive() {
        return new Mqtt5ClientImpl(buildClientData());
    }

    private Mqtt5ClientDataImpl buildClientData() {
        return new Mqtt5ClientDataImpl(identifier, host, port, followRedirects, allowServerReAuth);
    }

}
