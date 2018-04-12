package org.mqttbee.mqtt.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientConnectionData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ServerConnectionData;
import org.mqttbee.mqtt.MqttClientData;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientDataView implements Mqtt3ClientData {

    private final MqttClientData wrapped;

    private Mqtt3ClientDataView(@NotNull final MqttClientData wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public Optional<MqttClientIdentifier> getClientIdentifier() {
        return wrapped.getClientIdentifier();
    }

    @NotNull
    @Override
    public String getServerHost() {
        return wrapped.getServerHost();
    }

    @Override
    public int getServerPort() {
        return wrapped.getServerPort();
    }

    @Override
    public boolean usesSSL() {
        return wrapped.usesSSL();
    }

    @NotNull
    @Override
    public MqttClientExecutorConfig getExecutorConfig() {
        return wrapped.getExecutorConfig();
    }

    @Override
    public boolean isConnecting() {
        return wrapped.isConnecting();
    }

    @Override
    public boolean isConnected() {
        return wrapped.isConnected();
    }

    @NotNull
    @Override
    public Optional<Mqtt3ClientConnectionData> getClientConnectionData() {
        return Optional.ofNullable(wrapped.getRawClientConnectionData());
    }

    @NotNull
    @Override
    public Optional<Mqtt3ServerConnectionData> getServerConnectionData() {
        return Optional.ofNullable(wrapped.getRawServerConnectionData());
    }

}
