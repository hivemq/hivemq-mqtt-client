package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderWithMessage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3PubCompEncoder extends MqttMessageEncoderWithMessage<MqttPubComp> {

    public static final MqttMessageEncoderProvider<MqttPubComp> PROVIDER = Mqtt3PubCompEncoder::new;

    public static final Mqtt3PubCompEncoder INSTANCE = new Mqtt3PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBCOMP.getCode() << 4;
    private static final int FIXED_HEADER_LENGTH = 2;
    private static final int ENCODED_LENGTH = FIXED_HEADER_LENGTH + 2;

    @Override
    public int encodedLength(final int maxPacketSize) {
        return ENCODED_LENGTH;
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        encodeFixedHeader(out);
        encodeVariableHeader(out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(FIXED_HEADER_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

}
