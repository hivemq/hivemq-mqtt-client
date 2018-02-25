package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.NO_PACKET_IDENTIFIER_QOS_0;

/**
 * Util for decoders of MQTT messages for different versions.
 *
 * @author Silvio Giebl
 */
public class MqttMessageDecoderUtil {

    private MqttMessageDecoderUtil() {
    }

    public static void checkFixedHeaderFlags(final int expected, final int actual) throws MqttDecoderException {
        if (expected != actual) {
            throw new MqttDecoderException("fixed header flags must be " + expected + " but were " + actual);
        }
    }

    public static void checkRemainingLength(final int expected, final int actual) throws MqttDecoderException {
        if (expected != actual) {
            throw new MqttDecoderException("remaining length must be " + expected + " but was " + actual);
        }
    }

    @NotNull
    public static MqttDecoderException remainingLengthTooShort() {
        return new MqttDecoderException("remaining length too short");
    }

    @NotNull
    public static MqttDecoderException malformedUTF8String(@NotNull final String name) {
        return new MqttDecoderException("malformed UTF-8 string for" + name);
    }

    @NotNull
    public static MqttDecoderException malformedTopic() {
        return new MqttDecoderException(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed topic");
    }

    @NotNull
    public static MqttQoS decodePublishQoS(final int flags, final boolean dup) throws MqttDecoderException {
        final MqttQoS qos = MqttQoS.fromCode((flags & 0b0110) >> 1);
        if (qos == null) {
            throw new MqttDecoderException("wrong QoS");
        }
        if ((qos == MqttQoS.AT_MOST_ONCE) && dup) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "DUP flag must be 0 if QoS is 0");
        }
        return qos;
    }

    public static int decodePublishPacketIdentifier(@NotNull final MqttQoS qos, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        if (qos == MqttQoS.AT_MOST_ONCE) {
            return NO_PACKET_IDENTIFIER_QOS_0;
        }
        if (in.readableBytes() < 2) {
            throw remainingLengthTooShort();
        }
        return in.readUnsignedShort();
    }

}
