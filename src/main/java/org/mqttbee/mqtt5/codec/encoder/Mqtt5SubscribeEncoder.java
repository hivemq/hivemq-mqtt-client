package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeWrapper;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodeVariableByteIntegerProperty;
import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.variableByteIntegerPropertyEncodedLength;
import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeProperty.SUBSCRIPTION_IDENTIFIER;
import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeEncoder extends Mqtt5WrappedMessageEncoder<Mqtt5SubscribeImpl, Mqtt5SubscribeWrapper> {

    public static final Function<Mqtt5SubscribeImpl, Mqtt5SubscribeEncoder> PROVIDER = Mqtt5SubscribeEncoder::new;

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    private Mqtt5SubscribeEncoder(@NotNull final Mqtt5SubscribeImpl message) {
        super(message);
    }

    @Override
    int calculateEncodedRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = message.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    @Override
    int calculateEncodedPropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @Override
    public Function<Mqtt5SubscribeWrapper, ? extends Mqtt5MessageWrapperEncoder<Mqtt5SubscribeWrapper>> wrap() {
        return Mqtt5SubscribeWrapperEncoder.PROVIDER;
    }


    public static class Mqtt5SubscribeWrapperEncoder extends Mqtt5MessageWrapperEncoder<Mqtt5SubscribeWrapper> {

        public static final Function<Mqtt5SubscribeWrapper, Mqtt5SubscribeWrapperEncoder> PROVIDER =
                Mqtt5SubscribeWrapperEncoder::new;

        private static final int FIXED_HEADER = (Mqtt5MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;

        private Mqtt5SubscribeWrapperEncoder(@NotNull final Mqtt5SubscribeWrapper wrapper) {
            super(wrapper);
        }

        @Override
        int additionalPropertyLength() {
            return variableByteIntegerPropertyEncodedLength(message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        }

        @Override
        public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
            final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeByte(FIXED_HEADER);
            Mqtt5DataTypes.encodeVariableByteInteger(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeShort(message.getPacketIdentifier());
            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            Mqtt5DataTypes.encodeVariableByteInteger(propertyLength(maximumPacketSize), out);
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
            encodeOmissibleProperties(maximumPacketSize, out);
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions =
                    message.getWrapped().getSubscriptions();
            for (int i = 0; i < subscriptions.size(); i++) {
                final Mqtt5SubscribeImpl.SubscriptionImpl subscription = subscriptions.get(i);

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
