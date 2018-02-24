package org.mqttbee.mqtt.codec.decoder.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt5.ioc.ChannelComponent;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt3DecoderTest extends AbstractMqttDecoderTest {

    private final MqttClientDataImpl clientData;

    AbstractMqtt3DecoderTest(@NotNull final MqttMessageDecoders decoders) {
        super(decoders);
        clientData = new MqttClientDataImpl(MqttVersion.MQTT_3_1_1,
                Objects.requireNonNull(MqttClientIdentifierImpl.from("test")), "localhost", 1883, false, false, false,
                null, 0);
    }

    @Override
    protected void createChannel() {
        super.createChannel();
        clientData.to(channel);
        ChannelComponent.create(channel, clientData);
        clientData.setClientConnectionData(new MqttClientConnectionDataImpl(10, Mqtt5Connect.NO_SESSION_EXPIRY,
                Mqtt5Connect.Restrictions.DEFAULT_RECEIVE_MAXIMUM, 0, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT,
                null, false, false, false, channel));
    }

}
