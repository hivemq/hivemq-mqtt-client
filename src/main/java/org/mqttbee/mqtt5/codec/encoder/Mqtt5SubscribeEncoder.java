package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;
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

    private static final int FIXED_HEADER = Mqtt5MessageType.SUBSCRIBE.getCode() << 4 + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        final int propertyLength = calculatePropertyLength(subscribeInternal);
        final int remainingLength = calculateRemainingLength(subscribeInternal, propertyLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(remainingLength, out);
        encodeVariableHeader(subscribeInternal, propertyLength, out);
        encodePayload(subscribeInternal, out);
    }

    private int calculateRemainingLength(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, final int propertyLength) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        int remainingLength = 2;

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = subscribe.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertyLength(@NotNull final Mqtt5SubscribeInternal subscribeInternal) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        int propertyLength = 0;

        final int subscriptionIdentifier = subscribeInternal.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            propertyLength += 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(subscriptionIdentifier);
        }

        propertyLength += Mqtt5UserProperty.encodedLength(subscribe.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            // TODO exception remaining size exceeded
        }

        return propertyLength;
    }

    private void encodeFixedHeader(final int remainingLength, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, final int propertyLength,
            @NotNull final ByteBuf out) {

        out.writeShort(subscribeInternal.getPacketIdentfier());
        encodeProperties(subscribeInternal, propertyLength, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5SubscribeInternal subscribeInternal, final int propertyLength,
            @NotNull final ByteBuf out) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        final int subscriptionIdentifier = subscribeInternal.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            out.writeByte(Mqtt5SubscribeProperty.SUBSCRIPTION_IDENTIFIER);
            Mqtt5DataTypes.encodeVariableByteInteger(subscriptionIdentifier, out);
        }

        Mqtt5UserProperty.encode(subscribe.getUserProperties(), out);
    }

    private void encodePayload(@NotNull final Mqtt5SubscribeInternal subscribeInternal, @NotNull final ByteBuf out) {
        final Mqtt5SubscribeImpl subscribe = subscribeInternal.getSubscribe();

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = subscribe.getSubscriptions();
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
