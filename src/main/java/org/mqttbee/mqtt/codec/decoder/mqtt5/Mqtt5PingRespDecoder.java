package org.mqttbee.mqtt.codec.decoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.ping.MqttPingRespImpl;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.disconnect;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.disconnectWrongFixedHeaderFlags;
import static org.mqttbee.mqtt.message.ping.MqttPingRespImpl.INSTANCE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PingRespDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;

    @Inject
    Mqtt5PingRespDecoder() {
    }

    @Override
    public MqttPingRespImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("PING", channel);
            return null;
        }

        if (in.readableBytes() != 0) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "PING must not have a variable header or payload",
                    channel);
            return null;
        }

        return INSTANCE;
    }

}
