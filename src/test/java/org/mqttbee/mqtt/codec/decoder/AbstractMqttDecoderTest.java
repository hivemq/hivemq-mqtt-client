package org.mqttbee.mqtt.codec.decoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class AbstractMqttDecoderTest {

    private final MqttMessageDecoders decoders;

    protected EmbeddedChannel channel;

    public AbstractMqttDecoderTest(@NotNull final MqttMessageDecoders decoders) {
        this.decoders = decoders;
    }

    @BeforeEach
    protected void setUp() {
        createChannel();
    }

    @AfterEach
    protected void tearDown() {
        channel.close();
    }

    protected void createChannel() {
        channel = new EmbeddedChannel(new MqttDecoder(decoders));
    }

    public static MqttPingRespDecoder createPingRespDecoder() {
        return new MqttPingRespDecoder();
    }

}
