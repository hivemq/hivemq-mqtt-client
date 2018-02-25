package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.MqttServerConnectionDataImpl;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.NewMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscriptionImpl;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.encodeVariableByteIntegerProperty;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.variableByteIntegerPropertyEncodedLength;
import static org.mqttbee.mqtt.message.subscribe.MqttSubscribeProperty.SUBSCRIPTION_IDENTIFIER;
import static org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeEncoder extends Mqtt5WrappedMessageEncoder<MqttSubscribeImpl, MqttSubscribeWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttSubscribeImpl, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
            PROVIDER = NewMqttWrappedMessageEncoderProvider.create(Mqtt5SubscribeEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscriptionImpl> subscriptions = message.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttSubscribeWrapper wrapper) {
        return Mqtt5SubscribeWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5SubscribeWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttSubscribeWrapper, MqttSubscribeImpl, MqttMessageEncoderProvider<MqttSubscribeWrapper>, Mqtt5SubscribeEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttSubscribeWrapper, MqttSubscribeImpl, Mqtt5SubscribeEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5SubscribeWrapperEncoder::new);

        private static final int FIXED_HEADER = (Mqtt5MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;

        @Override
        int additionalPropertyLength() {
            return variableByteIntegerPropertyEncodedLength(message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        }

        @Override
        public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
            final int maximumPacketSize = MqttServerConnectionDataImpl.getMaximumPacketSize(channel);

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeByte(FIXED_HEADER);
            MqttVariableByteInteger.encode(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeShort(message.getPacketIdentifier());
            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            MqttVariableByteInteger.encode(propertyLength(maximumPacketSize), out);
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
            encodeOmissibleProperties(maximumPacketSize, out);
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ImmutableList<MqttSubscriptionImpl> subscriptions = message.getWrapped().getSubscriptions();
            for (int i = 0; i < subscriptions.size(); i++) {
                final MqttSubscriptionImpl subscription = subscriptions.get(i);

                subscription.getTopicFilter().to(out);

                int subscriptionOptions = 0;
                subscriptionOptions |= subscription.getRetainHandling().getCode() << 4;
                if (subscription.isRetainAsPublished()) {
                    subscriptionOptions |= 0b0000_1000;
                }
                if (subscription.isNoLocal()) {
                    subscriptionOptions |= 0b0000_0100;
                }
                subscriptionOptions |= subscription.getQoS().getCode();

                out.writeByte(subscriptionOptions);
            }
        }

    }

}
