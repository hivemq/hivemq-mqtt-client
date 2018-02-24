package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt5.netty.ChannelAttributes;
import org.mqttbee.util.ByteBufferUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3PublishDecoder implements MqttMessageDecoder {

    private static final int MIN_REMAINING_LENGTH = 2; // 2 for the packetIdentifier

    @Inject
    Mqtt3PublishDecoder() {
    }

    @Nullable
    @Override
    public MqttPublishWrapper decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        final boolean dup = (flags & 0b1000) != 0;
        final int code = (flags & 0b0110) >> 1;
        final MqttQoS qos = MqttQoS.fromCode(code);
        if (qos == null) {
            channel.close(); // TODO
            return null;
        }

        final boolean isRetain = (flags & 0b0001) != 0;

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            channel.close(); // TODO
            return null;
        }

        final MqttTopicImpl topic = MqttTopicImpl.from(in);
        if (topic == null) {
            channel.close(); // TODO
            return null;
        }

        final int packetIdentifier;
        if (qos != MqttQoS.AT_MOST_ONCE) {
            if (in.readableBytes() < 2) {
                channel.close(); // TODO
                return null;
            }
            packetIdentifier = in.readUnsignedShort();
        } else {
            packetIdentifier = MqttPublishWrapper.NO_PACKET_IDENTIFIER_QOS_0;
        }

        final int payloadLength = in.readableBytes();
        ByteBuffer payload = null;
        if (payloadLength > 0) {
            payload = ByteBufferUtil.allocate(payloadLength, ChannelAttributes.useDirectBufferForPayload(channel));
            in.readBytes(payload);
            payload.position(0);
        }

        final MqttPublishImpl publish = Mqtt3PublishView.wrapped(topic, payload, qos, isRetain);

        return Mqtt3PublishView.wrapped(publish, packetIdentifier, dup);
    }

}
