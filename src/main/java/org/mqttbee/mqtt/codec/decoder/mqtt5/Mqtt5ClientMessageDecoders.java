package org.mqttbee.mqtt.codec.decoder.mqtt5;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.message.MqttMessageType;

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

        decoders = new MqttMessageDecoder[MqttMessageType.values().length];
        decoders[MqttMessageType.CONNACK.getCode()] = connAckDecoder;
        decoders[MqttMessageType.PUBLISH.getCode()] = publishDecoder;
        decoders[MqttMessageType.PUBACK.getCode()] = pubAckDecoder;
        decoders[MqttMessageType.PUBREC.getCode()] = pubRecDecoder;
        decoders[MqttMessageType.PUBREL.getCode()] = pubRelDecoder;
        decoders[MqttMessageType.PUBCOMP.getCode()] = pubCompDecoder;
        decoders[MqttMessageType.SUBACK.getCode()] = subAckDecoder;
        decoders[MqttMessageType.UNSUBACK.getCode()] = unsubAckDecoder;
        decoders[MqttMessageType.PINGRESP.getCode()] = pingRespDecoder;
        decoders[MqttMessageType.DISCONNECT.getCode()] = disconnectDecoder;
        decoders[MqttMessageType.AUTH.getCode()] = authDecoder;
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
