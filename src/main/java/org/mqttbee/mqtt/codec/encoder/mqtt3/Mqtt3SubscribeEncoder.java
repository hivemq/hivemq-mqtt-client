package org.mqttbee.mqtt.codec.encoder.mqtt3;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.ThreadLocalMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscribeEncoder extends Mqtt3WrappedMessageEncoder<MqttSubscribe, MqttSubscribeWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttSubscribe, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
            PROVIDER = ThreadLocalMqttWrappedMessageEncoderProvider.create(Mqtt3SubscribeEncoder::new);

    private static final int FIXED_HEADER = (Mqtt3MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    int calculateRemainingLength() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscription> subscriptions = wrapped.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);
            remainingLength += subscription.getTopicFilter().encodedLength() + 1; // QoS
        }

        return remainingLength;
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        encodeFixedHeader(out);
        encodeVariableHeader(out);
        encodePayload(out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

    private void encodePayload(@NotNull final ByteBuf out) {
        final ImmutableList<MqttSubscription> subscriptions = wrapped.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);
            subscription.getTopicFilter().to(out);
            out.writeByte(subscription.getQoS().getCode());
        }
    }

}
