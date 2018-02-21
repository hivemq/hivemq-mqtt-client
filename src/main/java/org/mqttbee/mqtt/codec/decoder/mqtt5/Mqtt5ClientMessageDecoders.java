package org.mqttbee.mqtt.codec.decoder.mqtt5;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ClientMessageDecoders implements MqttMessageDecoders {

    private final MqttMessageDecoder[] decoders;

    @Inject
    Mqtt5ClientMessageDecoders(
            final Mqtt5ConnAckDecoder connAckDecoder, final Mqtt5PublishDecoder publishDecoder,
            final Mqtt5PubAckDecoder pubAckDecoder, final Mqtt5PubRecDecoder pubRecDecoder,
            final Mqtt5PubRelDecoder pubRelDecoder, final Mqtt5PubCompDecoder pubCompDecoder,
            final Mqtt5SubAckDecoder subAckDecoder, final Mqtt5UnsubAckDecoder unsubAckDecoder,
            final Mqtt5PingRespDecoder pingRespDecoder, final Mqtt5DisconnectDecoder disconnectDecoder,
            final Mqtt5AuthDecoder authDecoder) {

        decoders = new MqttMessageDecoder[Mqtt5MessageType.values().length];
        decoders[Mqtt5MessageType.CONNACK.getCode()] = connAckDecoder;
        decoders[Mqtt5MessageType.PUBLISH.getCode()] = publishDecoder;
        decoders[Mqtt5MessageType.PUBACK.getCode()] = pubAckDecoder;
        decoders[Mqtt5MessageType.PUBREC.getCode()] = pubRecDecoder;
        decoders[Mqtt5MessageType.PUBREL.getCode()] = pubRelDecoder;
        decoders[Mqtt5MessageType.PUBCOMP.getCode()] = pubCompDecoder;
        decoders[Mqtt5MessageType.SUBACK.getCode()] = subAckDecoder;
        decoders[Mqtt5MessageType.UNSUBACK.getCode()] = unsubAckDecoder;
        decoders[Mqtt5MessageType.PINGRESP.getCode()] = pingRespDecoder;
        decoders[Mqtt5MessageType.DISCONNECT.getCode()] = disconnectDecoder;
        decoders[Mqtt5MessageType.AUTH.getCode()] = authDecoder;
    }

    @Nullable
    @Override
    public MqttMessageDecoder get(final int code) {
        if (code < 0 || code >= decoders.length) {
            return null;
        }
        return decoders[code];
    }

}
