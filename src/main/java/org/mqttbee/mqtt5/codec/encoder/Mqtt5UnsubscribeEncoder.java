package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
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

    public static final Mqtt5UnsubscribeEncoder INSTANCE = new Mqtt5UnsubscribeEncoder();

    private static final int FIXED_HEADER = (Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4) + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(unsubscribeInternal, out);
        encodeVariableHeader(unsubscribeInternal, out);
        encodePayload(unsubscribeInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal) {
        final Mqtt5UnsubscribeImpl unsubscribe = unsubscribeInternal.getUnsubscribe();

        int remainingLength = 2;

        final int propertyLength = unsubscribeInternal.encodedPropertyLength();
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        final ImmutableList<Mqtt5TopicFilter> topicFilters = unsubscribe.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal) {
        final int propertyLength =
                Mqtt5UserProperty.encodedLength(unsubscribeInternal.getUnsubscribe().getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final ByteBuf out) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(unsubscribeInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final ByteBuf out) {

        out.writeShort(unsubscribeInternal.getPacketIdentifier());
        encodeProperties(unsubscribeInternal, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(unsubscribeInternal.encodedPropertyLength(), out);
        Mqtt5UserProperty.encode(unsubscribeInternal.getUnsubscribe().getUserProperties(), out);
    }

    private void encodePayload(
            @NotNull final Mqtt5UnsubscribeInternal unsubscribeInternal, @NotNull final ByteBuf out) {

        final ImmutableList<Mqtt5TopicFilter> topicFilters = unsubscribeInternal.getUnsubscribe().getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            topicFilters.get(i).to(out);
        }
    }

}
