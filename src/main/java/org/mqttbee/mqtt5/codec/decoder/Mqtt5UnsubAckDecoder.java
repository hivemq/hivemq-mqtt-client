package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckImpl;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckInternal;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckProperty;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Override
    @Nullable
    public Mqtt5UnsubAckInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("UNSUBACK", channel, in);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel, in);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel, in);
            return null;
        }
        if (in.readableBytes() < propertyLength) {
            disconnectRemainingLengthTooShort(channel, in);
            return null;
        }

        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel, in);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5UnsubAckProperty.REASON_STRING:
                    reasonString = decodeUTF8StringOnlyOnce(reasonString, "reason string", channel, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case Mqtt5UnsubAckProperty.USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("UNSUBACK", channel, in);
                    return null;
            }
        }

        if (readPropertyLength != propertyLength) {
            disconnectMalformedPropertyLength(channel, in);
            return null;
        }

        final int reasonCodeCount = in.readableBytes();
        if (reasonCodeCount == 0) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "UNSUBACK must contain at least one reason code",
                    channel, in);
        }

        final ImmutableList.Builder<Mqtt5UnsubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builderWithExpectedSize(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5UnsubAckReasonCode reasonCode = Mqtt5UnsubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("UNSUBACK", channel, in);
                return null;
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(userPropertiesBuilder);

        final Mqtt5UnsubAckImpl subAck = new Mqtt5UnsubAckImpl(reasonCodes, reasonString, userProperties);

        return new Mqtt5UnsubAckInternal(subAck, packetIdentifier);
    }

}
