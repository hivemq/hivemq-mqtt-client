package org.mqttbee.api;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.mqtt.MqttBuilderUtil;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder {

    private MqttClientIdentifierImpl identifier = MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private String serverHost = "localhost";
    private int serverPort = 1883;
    private boolean usesSSL;
    private Executor executor;
    private int numberOfNettyThreads;

    MqttClientBuilder() {
    }

    @NotNull
    public MqttClientBuilder withIdentifier(@NotNull final String identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    @NotNull
    public MqttClientBuilder withIdentifier(@NotNull final MqttClientIdentifier identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    @NotNull
    public MqttClientBuilder forServerHost(@NotNull final String host) {
        this.serverHost = host;
        return this;
    }

    @NotNull
    public MqttClientBuilder forServerPort(final int port) {
        this.serverPort = port;
        return this;
    }

    @NotNull
    public MqttClientBuilder usingSSL(final boolean usesSSl) {
        this.usesSSL = usesSSl;
        return this;
    }

    @NotNull
    public MqttClientBuilder usingExecutor(@NotNull final Executor executor) {
        Preconditions.checkNotNull(executor);
        this.executor = executor;
        return this;
    }

    @NotNull
    public MqttClientBuilder usingNettyThreads(final int numberOfNettyThreads) {
        Preconditions.checkArgument(numberOfNettyThreads > 0);
        this.numberOfNettyThreads = numberOfNettyThreads;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder usingMqtt5() {
        return new Mqtt5ClientBuilder(identifier, serverHost, serverPort, usesSSL, executor, numberOfNettyThreads);
    }

}
