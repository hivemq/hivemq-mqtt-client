package org.mqttbee.api.mqtt.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQoS2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQoS2ControlProvider;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AdvancedClientDataBuilder {

    private Mqtt5IncomingQoS1ControlProvider incomingQoS1ControlProvider;
    private Mqtt5OutgoingQoS1ControlProvider outgoingQoS1ControlProvider;
    private Mqtt5IncomingQoS2ControlProvider incomingQoS2ControlProvider;
    private Mqtt5OutgoingQoS2ControlProvider outgoingQoS2ControlProvider;

    @NotNull
    public Mqtt5AdvancedClientDataBuilder withIncomingQoS1ControlProvider(
            @NotNull final Mqtt5IncomingQoS1ControlProvider incomingQoS1ControlProvider) {

        this.incomingQoS1ControlProvider = incomingQoS1ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder getOutgoingQoS1ControlProvider(
            @NotNull final Mqtt5OutgoingQoS1ControlProvider outgoingQoS1ControlProvider) {

        this.outgoingQoS1ControlProvider = outgoingQoS1ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder getIncomingQoS2ControlProvider(
            @NotNull final Mqtt5IncomingQoS2ControlProvider incomingQoS2ControlProvider) {

        this.incomingQoS2ControlProvider = incomingQoS2ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder getOutgoingQoS2ControlProvider(
            @NotNull final Mqtt5OutgoingQoS2ControlProvider outgoingQoS2ControlProvider) {

        this.outgoingQoS2ControlProvider = outgoingQoS2ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientData builder() {
        return new MqttAdvancedClientData(incomingQoS1ControlProvider, outgoingQoS1ControlProvider,
                incomingQoS2ControlProvider, outgoingQoS2ControlProvider);
    }

}
