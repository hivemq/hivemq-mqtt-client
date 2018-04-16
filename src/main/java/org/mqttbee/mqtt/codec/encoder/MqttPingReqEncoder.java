package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class MqttPingReqEncoder implements MqttMessageEncoder {

    public static final MqttPingReqEncoder INSTANCE = new MqttPingReqEncoder();

    private static final int ENCODED_LENGTH = 2;
    private static final ByteBuf PACKET =
            Unpooled.directBuffer(ENCODED_LENGTH).writeByte(Mqtt5MessageType.PINGREQ.getCode() << 4).writeByte(0);

    @Override
    public int encodedLength(final int maxPacketSize) {
        return ENCODED_LENGTH;
    }

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        return PACKET.retainedDuplicate();
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        // no op
    }

}
