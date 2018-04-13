package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderWithMessage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRelEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3PubRelEncoder extends MqttMessageEncoderWithMessage<MqttPubRel> {

    public static final MqttPubRelEncoderProvider PROVIDER =
            new MqttPubRelEncoderProvider(Mqtt3PubRelEncoder::new, Mqtt3PubCompEncoder.PROVIDER);

    public static final Mqtt3PubRelEncoder INSTANCE = new Mqtt3PubRelEncoder();

    private static final int FIXED_HEADER = (Mqtt3MessageType.PUBREL.getCode() << 4) | 0b0010;
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
