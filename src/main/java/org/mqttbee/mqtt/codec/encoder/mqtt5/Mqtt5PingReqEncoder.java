package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageType;
import org.mqttbee.mqtt.message.ping.MqttPingReqImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingReqEncoder extends MqttMessageEncoder<MqttPingReqImpl> {

    private static final Mqtt5PingReqEncoder INSTANCE = new Mqtt5PingReqEncoder();
    public static final MqttMessageEncoderProvider<MqttPingReqImpl> PROVIDER = () -> INSTANCE;

    private static final int ENCODED_LENGTH = 2;
    private static final ByteBuf PACKET =
            Unpooled.directBuffer(ENCODED_LENGTH).writeByte(MqttMessageType.PINGREQ.getCode() << 4).writeByte(0);

    @Override
    public int encodedLength(final int maxPacketSize) {
        return ENCODED_LENGTH;
    }

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        return PACKET;
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        // no op
    }

}
