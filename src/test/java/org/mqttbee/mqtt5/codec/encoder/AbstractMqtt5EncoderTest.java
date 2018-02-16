package org.mqttbee.mqtt5.codec.encoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5ServerConnectionDataImpl;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
class AbstractMqtt5EncoderTest {

    private final boolean connected;
    private final Mqtt5ClientDataImpl clientData;

    EmbeddedChannel channel;

    AbstractMqtt5EncoderTest(final boolean connected) {
        this.connected = connected;
        clientData =
                new Mqtt5ClientDataImpl(Objects.requireNonNull(Mqtt5ClientIdentifierImpl.from("test")), "localhost",
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

    private void createChannel() {
        channel = new EmbeddedChannel(new Mqtt5Encoder());
        clientData.to(channel);
        if (connected) {
            createServerConnectionData(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
        }
    }

    void createServerConnectionData(final int maximumPacketSize) {
        clientData.setServerConnectionData(
                new Mqtt5ServerConnectionDataImpl(10, maximumPacketSize, 3, Mqtt5QoS.EXACTLY_ONCE, true, true, true,
                        true));
    }

}
