package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkRemainingLength;
import static org.mqttbee.mqtt.codec.decoder.mqtt3.Mqtt3MessageDecoderUtil.wrongReturnCode;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ConnAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    @Inject
    Mqtt3ConnAckDecoder() {
    }

    @Nullable
    @Override
    public MqttConnAck decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);
        checkRemainingLength(REMAINING_LENGTH, in.readableBytes());

        final byte connAckFlags = in.readByte();

        if ((connAckFlags & 0xfe) != 0) {
            throw new MqttDecoderException("wrong CONNACK flags, bits 7-1 must be 0");
        }

        final boolean sessionPresent = (connAckFlags & 0b1) == 1;

        final Mqtt3ConnAckReturnCode returnCode = Mqtt3ConnAckReturnCode.fromCode(in.readUnsignedByte());
        if (returnCode == null) {
            throw wrongReturnCode();
        }

        if ((returnCode != Mqtt3ConnAckReturnCode.SUCCESS) && sessionPresent) {
            throw new MqttDecoderException("session present must be 0 if return code is not SUCCESS");
        }

        return Mqtt3ConnAckView.wrapped(returnCode, sessionPresent);
    }

}
