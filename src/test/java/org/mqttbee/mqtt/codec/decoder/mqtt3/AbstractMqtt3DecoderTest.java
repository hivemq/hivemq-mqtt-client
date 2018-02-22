package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt3DecoderTest {

    private final MqttMessageDecoders decoders;
    private final Mqtt5ClientDataImpl clientData;

    EmbeddedChannel channel;

    AbstractMqtt3DecoderTest(@NotNull final MqttMessageDecoders decoders) {
        this.decoders = decoders;
        clientData = new Mqtt5ClientDataImpl(Objects.requireNonNull(MqttClientIdentifierImpl.from("test")), "localhost",
                1883, false, false, false, null, 0);
    }

    @BeforeEach
    void setUp() {
        createChannel();
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    void createChannel() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(decoders));
        clientData.to(channel);
        createClientConnectionData(Mqtt5Connect.Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
    }

    void createClientConnectionData(final int maximumPacketSize) {
        clientData.setClientConnectionData(
                new Mqtt5ClientConnectionDataImpl(10, 10, Mqtt5Connect.Restrictions.DEFAULT_RECEIVE_MAXIMUM, 3,
                        maximumPacketSize, null, false, true, true, channel));
    }

}
