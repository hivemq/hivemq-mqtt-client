package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
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

    public int encodedRemainingLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += encodedLengthWithHeader(subscribeInternal.encodedPropertyLength());

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions =
                subscribeInternal.getSubscribe().getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length"); // TODO
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        int propertyLength = 0;

        propertyLength += variableByteIntegerPropertyEncodedLength(subscribeInternal.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        propertyLength += subscribe.getUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length"); // TODO
        }
        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(subscribeInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeShort(subscribeInternal.getPacketIdentifier());
        encodeProperties(subscribeInternal, out);
    }

    private void encodeProperties(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        Mqtt5DataTypes.encodeVariableByteInteger(subscribeInternal.encodedPropertyLength(), out);

        encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscribeInternal.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
        subscribeInternal.getSubscribe().getUserProperties().encode(out);
    }

    private void encodePayload(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions =
                subscribeInternal.getSubscribe().getSubscriptions();
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
