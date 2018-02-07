package org.mqttbee.mqtt5.codec.decoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt5.handler.Mqtt5ClientData;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest {

    private final Mqtt5MessageDecoders decoders;

    EmbeddedChannel channel;

    AbstractMqtt5DecoderTest(@NotNull final Mqtt5MessageDecoders decoders) {
        this.decoders = decoders;
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
        channel = new EmbeddedChannel(new Mqtt5Decoder(decoders));
        createClientData(Mqtt5Connect.Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
    }

    void createClientData(final int maximumPacketSize) {
        new Mqtt5ClientData(Objects.requireNonNull(Mqtt5ClientIdentifierImpl.from("test")), 10, 10,
                Mqtt5Connect.Restrictions.DEFAULT_RECEIVE_MAXIMUM, 3, maximumPacketSize, null, false, true, channel);
    }

}
