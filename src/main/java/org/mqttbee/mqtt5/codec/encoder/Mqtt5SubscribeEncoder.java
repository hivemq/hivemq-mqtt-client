package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodeVariableByteIntegerProperty;
import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.variableByteIntegerPropertyEncodedLength;
import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeProperty.SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubscribeEncoder implements Mqtt5MessageEncoder<Mqtt5SubscribeInternal> {

    public static final Mqtt5SubscribeEncoder INSTANCE = new Mqtt5SubscribeEncoder();

    private static final int FIXED_HEADER = (Mqtt5MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(subscribeInternal, out);
        encodeVariableHeader(subscribeInternal, out);
        encodePayload(subscribeInternal, out);
    }

    public int encodedRemainingLengthWithoutProperties(@NotNull final Mqtt5SubscribeImpl subscribe) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = subscribe.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5SubscribeImpl subscribe) {
        return subscribe.getUserProperties().encodedLength();
    }

    public int additionalPropertyLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        return variableByteIntegerPropertyEncodedLength(
                subscribeInternal.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(
                subscribeInternal.encodedRemainingLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT), out); // TODO
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeShort(subscribeInternal.getPacketIdentifier());
        encodeProperties(subscribeInternal, out);
    }

    private void encodeProperties(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        Mqtt5DataTypes.encodeVariableByteInteger(
                subscribeInternal.encodedPropertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT), out); // TODO

        encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscribeInternal.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
        subscribeInternal.encodeUserProperties(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out); // TODO
    }

    private void encodePayload(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions =
                subscribeInternal.getWrapped().getSubscriptions();
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
