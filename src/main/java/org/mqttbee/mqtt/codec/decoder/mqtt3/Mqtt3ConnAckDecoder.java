package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    public MqttConnAckImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            channel.close(); // TODO
            return null;
        }

        if (in.readableBytes() != REMAINING_LENGTH) {
            channel.close(); // TODO
            return null;
        }

        final byte connAckFlags = in.readByte();

        if ((connAckFlags & 0xfe) != 0) {
            channel.close(); // TODO
            return null;
        }

        final boolean sessionPresent = (connAckFlags & 0b1) == 1;

        final Mqtt3ConnAckReturnCode returnCode = Mqtt3ConnAckReturnCode.fromCode(in.readUnsignedByte());
        if (returnCode == null) {
            channel.close(); // TODO
            return null;
        }

        return Mqtt3ConnAckView.wrapped(returnCode, sessionPresent);
    }

}
