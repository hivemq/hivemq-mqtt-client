package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;

import java.nio.ByteBuffer;

/**
 * Util for encoders of MQTT messages for different versions.
 *
 * @author Silvio Giebl
 */
public class MqttMessageEncoderUtil {

    private MqttMessageEncoderUtil() {
    }

    /**
     * Calculates the encoded length of a MQTT message with the given remaining length.
     *
     * @param remainingLength the remaining length of the MQTT message.
     * @return the encoded length of the MQTT message.
     */
    public static int encodedPacketLength(final int remainingLength) {
        return 1 + encodedLengthWithHeader(remainingLength);
    }

    /**
     * Calculates the encoded length with a prefixed header.
     *
     * @param encodedLength the encoded length.
     * @return the encoded length with a prefixed header.
     */
    public static int encodedLengthWithHeader(final int encodedLength) {
        return MqttVariableByteInteger.encodedLength(encodedLength) + encodedLength;
    }

    public static int nullableEncodedLength(@Nullable final MqttUTF8StringImpl string) {
        return (string == null) ? 0 : string.encodedLength();
    }

    public static int nullableEncodedLength(@Nullable final ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? 0 : MqttBinaryData.encodedLength(byteBuffer);
    }

    public static int encodedOrEmptyLength(@Nullable final ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? MqttBinaryData.EMPTY_LENGTH : MqttBinaryData.encodedLength(byteBuffer);
    }

    public static void encodeNullable(@Nullable final MqttUTF8StringImpl string, @NotNull final ByteBuf out) {
        if (string != null) {
            string.to(out);
        }
    }

    public static void encodeNullable(@Nullable final ByteBuffer byteBuffer, @NotNull final ByteBuf out) {
        if (byteBuffer != null) {
            MqttBinaryData.encode(byteBuffer, out);
        }
    }

    public static void encodeOrEmpty(@Nullable final ByteBuffer byteBuffer, @NotNull final ByteBuf out) {
        if (byteBuffer != null) {
            MqttBinaryData.encode(byteBuffer, out);
        } else {
            MqttBinaryData.encodeEmpty(out);
        }
    }

}
