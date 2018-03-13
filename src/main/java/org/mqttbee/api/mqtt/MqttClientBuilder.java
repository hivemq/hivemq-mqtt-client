package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder {

    private MqttClientIdentifierImpl identifier = MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private String serverHost = "localhost";
    private int serverPort = 1883;
    private boolean usesSSL;
    private MqttClientExecutorConfigImpl executorConfig = MqttClientExecutorConfigImpl.DEFAULT;

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
    public MqttClientBuilder usingExecutorConfig(@NotNull final MqttClientExecutorConfig executorConfig) {
        this.executorConfig =
                MustNotBeImplementedUtil.checkNotImplemented(executorConfig, MqttClientExecutorConfigImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder usingMqtt5() {
        return new Mqtt5ClientBuilder(identifier, serverHost, serverPort, usesSSL, executorConfig);
    }

}
