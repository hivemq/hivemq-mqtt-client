package org.mqttbee.mqtt3.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.suback.Mqtt3SubAckImpl;
import org.mqttbee.mqtt3.message.suback.Mqtt3SubAckReasonCode;

import java.util.ArrayList;
import java.util.List;

public class Mqtt3SubAckDecoder implements Mqtt3MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3; // 2 for the packetId + 1 for at least one Subscription

    @Nullable
    @Override
    public Mqtt3SubAckImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if (flags != FLAGS) {
            channel.close();
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            channel.close();
            return null;
        }

        final int packetID = in.readUnsignedShort();
        final int subscriptions = in.readableBytes();
        final List<Mqtt3SubAckReasonCode> subscriptionsAcks = new ArrayList<>();

        for (int i = 0; i < subscriptions; i++) {
            final Mqtt3SubAckReasonCode ackReturnCode = Mqtt3SubAckReasonCode.from(in.readUnsignedByte());
            if (ackReturnCode == null) {
                channel.close();
                return null;
            }
            subscriptionsAcks.add(ackReturnCode);
        }
        final ImmutableList<Mqtt3SubAckReasonCode> reasonCodes = ImmutableList.copyOf(subscriptionsAcks);
        return new Mqtt3SubAckImpl(packetID, reasonCodes);
    }

}
