package org.mqttbee.mqtt.codec.decoder.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt5.ioc.ChannelComponent;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest extends AbstractMqttDecoderTest {

    private final MqttClientDataImpl clientData;

    public AbstractMqtt5DecoderTest(@NotNull final MqttMessageDecoders decoders) {
        super(decoders);
        clientData = new MqttClientDataImpl(MqttVersion.MQTT_5_0,
                Objects.requireNonNull(MqttClientIdentifierImpl.from("test")), "localhost", 1883, false, false, false,
                MqttClientExecutorConfigImpl.DEFAULT);
    }

    @Override
    protected void createChannel() {
        super.createChannel();
        clientData.to(channel);
        ChannelComponent.create(channel, clientData);
        createClientConnectionData(Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
    }

    void createClientConnectionData(final int maximumPacketSize) {
        clientData.setClientConnectionData(
                new MqttClientConnectionDataImpl(10, 10, Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, 3,
                        maximumPacketSize, null, false, true, true, channel));
    }

}
