package org.mqttbee.api;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.Mqtt5BuilderUtil;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder {

    private Mqtt5ClientIdentifierImpl identifier = Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private String host = "localhost";
    private int port = 1883;
    private Executor executor;
    private int numberOfNettyThreads;

    MqttClientBuilder() {
    }

    @NotNull
    public MqttClientBuilder withIdentifier(@NotNull final String identifier) {
        this.identifier = Mqtt5BuilderUtil.clientIdentifier(identifier);
        return this;
    }

    @NotNull
    public MqttClientBuilder withIdentifier(@NotNull final Mqtt5ClientIdentifier identifier) {
        this.identifier = Mqtt5BuilderUtil.clientIdentifier(identifier);
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
    public MqttClientBuilder using(@NotNull final Executor executor) {
        Preconditions.checkNotNull(executor);
        this.executor = executor;
        return this;
    }

    @NotNull
    public MqttClientBuilder using(final int numberOfNettyThreads) {
        Preconditions.checkArgument(numberOfNettyThreads > 0);
        this.numberOfNettyThreads = numberOfNettyThreads;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder useMqtt5() {
        return new Mqtt5ClientBuilder(identifier, host, port, executor, numberOfNettyThreads);
    }

}
