package org.mqttbee.mqtt.codec.decoder.mqtt3;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAckImpl;
import org.mqttbee.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3SubAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3; // 2 for the packetId + 1 for at least one Subscription

    @Inject
    Mqtt3SubAckDecoder() {
    }

    @Nullable
    @Override
    public MqttSubAckImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            channel.close(); // TODO
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            channel.close(); // TODO
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int returnCodeCount = in.readableBytes();
        final ImmutableList.Builder<Mqtt3SubAckReturnCode> returnCodesBuilder = ImmutableList.builder();
        for (int i = 0; i < returnCodeCount; i++) {
            final Mqtt3SubAckReturnCode returnCode = Mqtt3SubAckReturnCode.from(in.readUnsignedByte());
            if (returnCode == null) {
                channel.close(); // TODO
                return null;
            }
            returnCodesBuilder.add(returnCode);
        }

        return Mqtt3SubAckView.wrapped(packetIdentifier, returnCodesBuilder.build());
    }

}
