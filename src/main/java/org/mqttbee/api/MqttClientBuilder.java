package org.mqttbee.api;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder {

    private Mqtt5ClientIdentifierImpl identifier = Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private String host = "localhost";
    private int port = 1883;

    @NotNull
    public MqttClientBuilder withIdentifier(@NotNull final Mqtt5ClientIdentifier identifier) {
        this.identifier = MustNotBeImplementedUtil.checkNotImplemented(identifier, Mqtt5ClientIdentifierImpl.class);
        return this;
    }

    @NotNull
    public MqttClientBuilder forServer(@NotNull final String host) {
        this.host = host;
        return this;
    }

    @NotNull
    public MqttClientBuilder forServerPort(final int port) {
        this.port = port;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder useMqtt5() {
        return new Mqtt5ClientBuilder(identifier, host, port);
    }

}
