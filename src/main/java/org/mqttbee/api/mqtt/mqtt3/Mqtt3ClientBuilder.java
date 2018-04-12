package org.mqttbee.api.mqtt.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientView;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientBuilder {

    private final MqttClientIdentifierImpl identifier;
    private final String serverHost;
    private final int serverPort;
    private final boolean usesSSL;
    private final MqttClientExecutorConfigImpl executorConfig;

    public Mqtt3ClientBuilder(
            @NotNull final MqttClientIdentifierImpl identifier, @NotNull final String serverHost, final int serverPort,
            final boolean usesSSL, @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        this.identifier = identifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.usesSSL = usesSSL;
        this.executorConfig = executorConfig;
    }

    @NotNull
    public Mqtt3Client reactive() {
        return new Mqtt3ClientView(new Mqtt5ClientImpl(buildClientData()));
    }

    private MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, usesSSL, false, false,
                executorConfig, null);
    }

}
