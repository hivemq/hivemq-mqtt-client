package org.mqttbee.mqtt5.codec;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5CodecUtil {

    private Mqtt5CodecUtil() {
    }

    public static boolean checkMaximumPacketSize(final int packetSize, @NotNull final Channel channel) {
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        return (maximumPacketSize == null) || (packetSize <= maximumPacketSize);
    }

}
