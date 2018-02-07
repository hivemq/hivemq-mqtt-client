package org.mqttbee.mqtt3.codec.decoder;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ClientMessageDecoders implements Mqtt3MessageDecoders {

    private final Mqtt3MessageDecoder[] decoders;

    @Inject
    Mqtt3ClientMessageDecoders(
            final Mqtt3ConnAckDecoder connAckDecoder, final Mqtt3PublishDecoder publishDecoder,
            final Mqtt3PubAckDecoder pubAckDecoder, final Mqtt3PubRecDecoder pubRecDecoder,
            final Mqtt3PubRelDecoder pubRelDecoder, final Mqtt3PubCompDecoder pubCompDecoder,
            final Mqtt3SubAckDecoder subAckDecoder, final Mqtt3UnsubAckDecoder unsubAckDecoder,
            final Mqtt3PingRespDecoder pingRespDecoder) {
        decoders = new Mqtt3MessageDecoder[Mqtt3MessageType.values().length];
        decoders[Mqtt3MessageType.CONNACK.getCode()] = connAckDecoder;
        decoders[Mqtt3MessageType.PUBLISH.getCode()] = publishDecoder;
        decoders[Mqtt3MessageType.PUBACK.getCode()] = pubAckDecoder;
        decoders[Mqtt3MessageType.PUBREC.getCode()] = pubRecDecoder;
        decoders[Mqtt3MessageType.PUBREL.getCode()] = pubRelDecoder;
        decoders[Mqtt3MessageType.PUBCOMP.getCode()] = pubCompDecoder;
        decoders[Mqtt3MessageType.SUBACK.getCode()] = subAckDecoder;
        decoders[Mqtt3MessageType.UNSUBACK.getCode()] = unsubAckDecoder;
        decoders[Mqtt3MessageType.PINGRESP.getCode()] = pingRespDecoder;
    }

    @Nullable
    @Override
    public Mqtt3MessageDecoder get(final int code) {
        if (code < 0 || code >= decoders.length) {
            return null;
        }
        return decoders[code];
    }

}
