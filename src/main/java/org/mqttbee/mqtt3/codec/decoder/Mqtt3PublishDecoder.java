package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishInternal;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;

import static org.mqttbee.mqtt3.codec.decoder.Mqtt3DecoderUtil.disconnectUngracefully;

public class Mqtt3PublishDecoder implements Mqtt3MessageDecoder {

    private static final int MIN_REMAINING_LENGTH = 2; // 2 for the packetId

    @Nullable
    @Override
    public Mqtt3Message decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        final boolean dup = (flags & 0b1000) != 0;
        final int code = (flags & 0b0110) >> 1;
        final Mqtt5QoS qos = Mqtt5QoS.fromCode(code); //Mqtt5QoS same as Mqtt3 will be renamed
        if (qos == null) {
            channel.close();
            return null;
        }

        final boolean isRetained = (flags & 0b0001) != 0;

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            channel.close();
            return null;
        }

        final Mqtt5TopicImpl topic = Mqtt5TopicImpl.from(in);
        if (topic == null) {
            // we must close the network connection in case of a null character and may close it if any other illegal character is contained
            disconnectUngracefully(channel);
            return null;
        }

        final int packetId;
        if (qos != Mqtt5QoS.AT_MOST_ONCE) {
            if (in.readableBytes() < 2) {
                // at least two more bytes are needed for the packet identifier + payload
                return null;
            }
            packetId = (int) in.readShort();
        } else {
            packetId = -1;
        }

        byte[] payload = null;
        final int remainingBytes = in.readableBytes();
        if (remainingBytes > 0) {
            payload = new byte[remainingBytes];
            in.readBytes(payload);
        }

        final Mqtt3PublishImpl publish = new Mqtt3PublishImpl(payload, topic, qos, isRetained, dup, packetId);
        return new Mqtt3PublishInternal(publish, packetId);
    }

}
