package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderWithMessage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRecEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3PubRecEncoder extends MqttMessageEncoderWithMessage<MqttPubRec> {

    public static final MqttPubRecEncoderProvider PROVIDER =
            new MqttPubRecEncoderProvider(Mqtt3PubRecEncoder::new, Mqtt3PubRelEncoder.PROVIDER);

    public static final Mqtt3PubRecEncoder INSTANCE = new Mqtt3PubRecEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBREC.getCode() << 4;
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
