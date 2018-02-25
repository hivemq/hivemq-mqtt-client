package org.mqttbee.mqtt.codec.decoder;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil.disconnect;

/**
 * @author Silvio Giebl
 */
public class MqttMessageDecoderUtil {

    private MqttMessageDecoderUtil() {
    }

    public static void disconnectWrongFixedHeaderFlags(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong fixed header flags for " + type);
    }

    public static void disconnectRemainingLengthTooShort(@NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "remaining length too short");
    }

    public static void disconnectMalformedUTF8String(@NotNull final String name, @NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed UTF-8 string for" + name);
    }

    public static void disconnectMustNotHavePayload(@NotNull final String type, @NotNull final Channel channel) {
        disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, type + " must not have a payload");
    }

}
