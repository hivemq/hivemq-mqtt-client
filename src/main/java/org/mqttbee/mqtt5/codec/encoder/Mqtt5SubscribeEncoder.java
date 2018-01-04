package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubscribeEncoder implements Mqtt5MessageEncoder<Mqtt5SubscribeInternal> {

    public static final Mqtt5SubscribeEncoder INSTANCE = new Mqtt5SubscribeEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.SUBSCRIBE.getCode() << 4 + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(subscribeInternal, out);
        encodeVariableHeader(subscribeInternal, out);
        encodePayload(subscribeInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        int remainingLength = 2;

        final int propertyLength = subscribeInternal.encodedPropertyLength();
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions =
                subscribeInternal.getSubscribe().getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        int propertyLength = 0;

        final int subscriptionIdentifier = subscribeInternal.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            propertyLength += 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(subscriptionIdentifier);
        }

        propertyLength += Mqtt5UserProperty.encodedLength(subscribe.getUserProperties());

        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(subscribeInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {

        out.writeShort(subscribeInternal.getPacketIdentfier());
        encodeProperties(subscribeInternal, out);
    }

    private void encodeProperties(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        Mqtt5DataTypes.encodeVariableByteInteger(subscribeInternal.encodedPropertyLength(), out);

        final int subscriptionIdentifier = subscribeInternal.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            out.writeByte(Mqtt5SubscribeProperty.SUBSCRIPTION_IDENTIFIER);
            Mqtt5DataTypes.encodeVariableByteInteger(subscriptionIdentifier, out);
        }

        Mqtt5UserProperty.encode(subscribeInternal.getSubscribe().getUserProperties(), out);
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
