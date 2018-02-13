package org.mqttbee.api.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder {

    private Mqtt5ClientIdentifierImpl identifier = Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private String host = "localhost";
    private int port = 1883;
    private boolean followRedirects = false;

    public Mqtt5ClientBuilder(
            @NotNull final Mqtt5ClientIdentifierImpl identifier, @NotNull final String host, final int port) {

        this.identifier = identifier;
        this.host = host;
        this.port = port;
    }

    @NotNull
    public Mqtt5ClientBuilder withIdentifier(@NotNull final Mqtt5ClientIdentifier identifier) {
        this.identifier = MustNotBeImplementedUtil.checkNotImplemented(identifier, Mqtt5ClientIdentifierImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder forServer(@NotNull final String host) {
        this.host = host;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder forServerPort(final int port) {
        this.port = port;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder followRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    @NotNull
    public Mqtt5Client reactive() {
        return null; // TODO
    }

}
