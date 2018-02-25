package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

/**
 * @author Silvio Giebl
 */
public class Mqtt3DisconnectEncoder implements MqttMessageEncoder {

    private static final Mqtt3DisconnectEncoder INSTANCE = new Mqtt3DisconnectEncoder();
    public static final MqttMessageEncoderProvider<MqttDisconnect> PROVIDER = () -> (message) -> INSTANCE;

    private static final int ENCODED_LENGTH = 2;
    private static final ByteBuf PACKET =
            Unpooled.directBuffer(ENCODED_LENGTH).writeByte(Mqtt3MessageType.DISCONNECT.getCode() << 4).writeByte(0);

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        // no op
    }

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        return PACKET.retainedDuplicate();
    }

    @Override
    public int encodedLength(final int maxPacketSize) {
        return ENCODED_LENGTH;
    }

}
