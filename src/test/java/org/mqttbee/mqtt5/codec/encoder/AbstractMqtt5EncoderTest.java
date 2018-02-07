package org.mqttbee.mqtt5.codec.encoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;

/**
 * @author Silvio Giebl
 */
class AbstractMqtt5EncoderTest {

    private final boolean connected;

    EmbeddedChannel channel;

    AbstractMqtt5EncoderTest(final boolean connected) {
        this.connected = connected;
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
        if (connected) {
            createServerData(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
        }
    }

    void createServerData(final int maximumPacketSize) {
        final Mqtt5ServerData serverData =
                new Mqtt5ServerData(10, maximumPacketSize, 3, Mqtt5QoS.EXACTLY_ONCE, true, true, true, true);
        serverData.set(channel);
    }

}
