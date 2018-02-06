package org.mqttbee.mqtt3.codec.decoder;

import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.codec.decoder.*;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3ClientMessageDecodersTest {

    @Test
    void test() {
        final Mqtt3ConnAckDecoder connAckDecoder = new Mqtt3ConnAckDecoder();
        final Mqtt3PublishDecoder publishDecoder = new Mqtt3PublishDecoder();
        final Mqtt3PubAckDecoder pubAckDecoder = new Mqtt3PubAckDecoder();
        final Mqtt3PubRecDecoder pubRecDecoder = new Mqtt3PubRecDecoder();
        final Mqtt3PubRelDecoder pubRelDecoder = new Mqtt3PubRelDecoder();
        final Mqtt3PubCompDecoder pubCompDecoder = new Mqtt3PubCompDecoder();
        final Mqtt3SubAckDecoder subAckDecoder = new Mqtt3SubAckDecoder();
        final Mqtt3UnsubAckDecoder unsubAckDecoder = new Mqtt3UnsubAckDecoder();
        final Mqtt3PingRespDecoder pingRespDecoder = new Mqtt3PingRespDecoder();

        final Mqtt3ClientMessageDecoders clientMessageDecoders =
                new Mqtt3ClientMessageDecoders(connAckDecoder, publishDecoder, pubAckDecoder, pubRecDecoder,
                        pubRelDecoder, pubCompDecoder, subAckDecoder, unsubAckDecoder, pingRespDecoder);
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
    }
}