package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.message.ping.MqttPingRespImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.disconnectWrongFixedHeaderFlags;
import static org.mqttbee.mqtt.message.ping.MqttPingRespImpl.INSTANCE;
import static org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil.disconnect;

/**
 * @author Silvio Giebl
 */
@Singleton
public class MqttPingRespDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;

    @Inject
    MqttPingRespDecoder() {
    }

    @Override
    public MqttPingRespImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags(channel, "PING");
            return null;
        }

        if (in.readableBytes() != 0) {
            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET,
                    "PING must not have a variable header or payload");
            return null;
        }

        return INSTANCE;
    }

}
