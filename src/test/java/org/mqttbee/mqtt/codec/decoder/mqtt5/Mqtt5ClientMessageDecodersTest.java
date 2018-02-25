package org.mqttbee.mqtt.codec.decoder.mqtt5;

import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.decoder.AbstractMqttDecoderTest;
import org.mqttbee.mqtt.codec.decoder.MqttPingRespDecoder;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Silvio Giebl
 */
class Mqtt5ClientMessageDecodersTest {

    @Test
    void test() {
        final Mqtt5ConnAckDecoder connAckDecoder = new Mqtt5ConnAckDecoder();
        final Mqtt5PublishDecoder publishDecoder = new Mqtt5PublishDecoder();
        final Mqtt5PubAckDecoder pubAckDecoder = new Mqtt5PubAckDecoder();
        final Mqtt5PubRecDecoder pubRecDecoder = new Mqtt5PubRecDecoder();
        final Mqtt5PubRelDecoder pubRelDecoder = new Mqtt5PubRelDecoder();
        final Mqtt5PubCompDecoder pubCompDecoder = new Mqtt5PubCompDecoder();
        final Mqtt5SubAckDecoder subAckDecoder = new Mqtt5SubAckDecoder();
        final Mqtt5UnsubAckDecoder unsubAckDecoder = new Mqtt5UnsubAckDecoder();
        final MqttPingRespDecoder pingRespDecoder = AbstractMqttDecoderTest.createPingRespDecoder();
        final Mqtt5DisconnectDecoder disconnectDecoder = new Mqtt5DisconnectDecoder();
        final Mqtt5AuthDecoder authDecoder = new Mqtt5AuthDecoder();

        final Mqtt5ClientMessageDecoders clientMessageDecoders =
                new Mqtt5ClientMessageDecoders(connAckDecoder, publishDecoder, pubAckDecoder, pubRecDecoder,
                        pubRelDecoder, pubCompDecoder, subAckDecoder, unsubAckDecoder, pingRespDecoder,
                        disconnectDecoder, authDecoder);

        assertNull(clientMessageDecoders.get(-1));
        assertNull(clientMessageDecoders.get(Mqtt5MessageType.values().length));
        assertNull(clientMessageDecoders.get(0));
        assertNull(clientMessageDecoders.get(1));
        assertSame(connAckDecoder, clientMessageDecoders.get(2));
        assertSame(publishDecoder, clientMessageDecoders.get(3));
        assertSame(pubAckDecoder, clientMessageDecoders.get(4));
        assertSame(pubRecDecoder, clientMessageDecoders.get(5));
        assertSame(pubRelDecoder, clientMessageDecoders.get(6));
        assertSame(pubCompDecoder, clientMessageDecoders.get(7));
        assertNull(clientMessageDecoders.get(8));
        assertSame(subAckDecoder, clientMessageDecoders.get(9));
        assertNull(clientMessageDecoders.get(10));
        assertSame(unsubAckDecoder, clientMessageDecoders.get(11));
        assertNull(clientMessageDecoders.get(12));
        assertSame(pingRespDecoder, clientMessageDecoders.get(13));
        assertSame(disconnectDecoder, clientMessageDecoders.get(14));
        assertSame(authDecoder, clientMessageDecoders.get(15));
    }

}