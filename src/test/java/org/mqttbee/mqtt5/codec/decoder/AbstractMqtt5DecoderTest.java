package org.mqttbee.mqtt5.codec.decoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5DecoderTest {

    private final Mqtt5MessageDecoders decoders;
    private final Mqtt5ClientDataImpl clientData;

    EmbeddedChannel channel;

    AbstractMqtt5DecoderTest(@NotNull final Mqtt5MessageDecoders decoders) {
        this.decoders = decoders;
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

    void createChannel() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(decoders));
        clientData.to(channel);
        createClientConnectionData(Mqtt5Connect.Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
    }

    void createClientConnectionData(final int maximumPacketSize) {
        clientData.setClientConnectionData(
                new Mqtt5ClientConnectionDataImpl(10, 10, Mqtt5Connect.Restrictions.DEFAULT_RECEIVE_MAXIMUM, 3,
                        maximumPacketSize, null, false, true, true, channel));
    }

}
