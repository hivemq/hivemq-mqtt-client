package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt5.netty.ChannelAttributes;
import org.mqttbee.util.ByteBufferUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.*;

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
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) throws MqttDecoderException {

        final Channel channel = clientConnectionData.getChannel();

        final boolean dup = (flags & 0b1000) != 0;
        final MqttQoS qos = decodePublishQoS(flags, dup);
        final boolean retain = (flags & 0b0001) != 0;

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final MqttTopicImpl topic = MqttTopicImpl.from(in);
        if (topic == null) {
            throw malformedTopic();
        }

        final int packetIdentifier = decodePublishPacketIdentifier(qos, in);

        final int payloadLength = in.readableBytes();
        ByteBuffer payload = null;
        if (payloadLength > 0) {
            payload = ByteBufferUtil.allocate(payloadLength, ChannelAttributes.useDirectBufferForPayload(channel));
            in.readBytes(payload);
            payload.position(0);
        }

        final MqttPublish publish = Mqtt3PublishView.wrapped(topic, payload, qos, retain);

        return Mqtt3PublishView.wrapped(publish, packetIdentifier, dup);
    }

}
