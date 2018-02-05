package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;

import static org.mqttbee.mqtt3.codec.decoder.Mqtt3DecoderUtil.disconnectUngracefully;

public class Mqtt3PublishDecoder implements Mqtt3MessageDecoder {


    @Nullable
    @Override
    public Mqtt3Message decode(
            int flags, @NotNull Channel channel, @NotNull ByteBuf in) {

        final boolean dup = (flags & 0b1000) != 0;
        final Mqtt5QoS qos = Mqtt5QoS.fromCode((flags & 0b0110) >> 1); //Mqtt5QoS same as Mqtt3 will be renamed
        final boolean isRetained = (flags & 0b0001) != 0;

        if (in.readableBytes() < 2) {
            return null;
        }

        Mqtt5Topic topic = Mqtt5Topic.from(in);
        if (topic == null) {
            // we must close the network connection in case of a null character and may close it if any other illegal character is contained
            disconnectUngracefully(channel);
        }

        if (in.readableBytes() < 2) {
            // 2 more bytes are needed for the packet identifier
            return null;
        }
        final int packetId = in.readShort();

        final int remainingBytes = in.readableBytes();

        if(remainingBytes > 0){
            final byte[] payloadBytes = new byte[remainingBytes];
            in.readBytes(payloadBytes);
            return new Mqtt3PublishImpl(payloadBytes, topic, qos, isRetained);
        }else{
            return new Mqtt3PublishImpl(null, topic, qos, isRetained);
        }
    }
}
