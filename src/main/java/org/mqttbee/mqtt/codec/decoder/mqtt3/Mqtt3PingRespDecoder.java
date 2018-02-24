package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.ping.MqttPingRespImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.message.ping.MqttPingRespImpl.INSTANCE;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3PingRespDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;

    @Inject
    Mqtt3PingRespDecoder() {
    }

    @Nullable
    @Override
    public MqttPingRespImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            channel.close(); // TODO
            return null;
        }

        if (in.readableBytes() != 0) {
            channel.close(); // TODO
            return null;
        }

        return INSTANCE;
    }

}

