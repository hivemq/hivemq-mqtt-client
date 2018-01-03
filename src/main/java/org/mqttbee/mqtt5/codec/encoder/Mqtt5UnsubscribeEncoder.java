package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeInternal;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubscribeEncoder implements Mqtt5MessageEncoder<Mqtt5UnsubscribeInternal> {

    private static final int FIXED_HEADER = Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4 + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        final int propertyLength = calculatePropertyLength(unsubscribeInternal);
        final int remainingLength = calculateRemainingLength(unsubscribeInternal, propertyLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(remainingLength, out);
        encodeVariableHeader(unsubscribeInternal, propertyLength, out);
        encodePayload(unsubscribeInternal, out);
    }

    private int calculateRemainingLength(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, final int propertyLength) {
        final Mqtt5UnsubscribeImpl unsubscribe = unsubscribeInternal.getUnsubscribe();

        int remainingLength = 2;

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        final ImmutableList<Mqtt5TopicFilter> topicFilters = unsubscribe.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertyLength(@NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal) {
        final Mqtt5UnsubscribeImpl unsubscribe = unsubscribeInternal.getUnsubscribe();

        final int propertyLength = Mqtt5UserProperty.encodedLength(unsubscribe.getUserProperties());

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
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, final int propertyLength,
            @NotNull final ByteBuf out) {

        out.writeShort(unsubscribeInternal.getPacketIdentifier());
        encodeProperties(unsubscribeInternal, propertyLength, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, final int propertyLength,
            @NotNull final ByteBuf out) {
        final Mqtt5UnsubscribeImpl unsubscribe = unsubscribeInternal.getUnsubscribe();

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);
        Mqtt5UserProperty.encode(unsubscribe.getUserProperties(), out);
    }

    private void encodePayload(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final ByteBuf out) {
        final Mqtt5UnsubscribeImpl unsubscribe = unsubscribeInternal.getUnsubscribe();

        final ImmutableList<Mqtt5TopicFilter> topicFilters = unsubscribe.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            topicFilters.get(i).to(out);
        }
    }

}
