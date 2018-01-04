package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Override
    @Nullable
    public Mqtt5PubAckInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("PUBACK", channel, in);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel, in);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubAckReasonCode reasonCode = DEFAULT_REASON_CODE;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("PUBACK", channel, in);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel, in);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("PUBACK", channel, in);
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        disconnectMalformedPropertyIdentifier(channel, in);
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5PubAckProperty.REASON_STRING:
                            reasonString = decodeUTF8StringOnlyOnce(reasonString, "reason string", channel, in);
                            if (reasonString == null) {
                                return null;
                            }
                            break;

                        case Mqtt5PubAckProperty.USER_PROPERTY:
                            userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                            if (userPropertiesBuilder == null) {
                                return null;
                            }
                            break;

                        default:
                            disconnectWrongProperty("PUBACK", channel, in);
                            return null;
                    }
                }
            }
        }

        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(userPropertiesBuilder);

        final Mqtt5PubAckImpl pubAck = new Mqtt5PubAckImpl(reasonCode, reasonString, userProperties);

        final Mqtt5PubAckInternal pubAckInternal = new Mqtt5PubAckInternal(pubAck);
        pubAckInternal.setPacketIdentifier(packetIdentifier);

        return pubAckInternal;
    }

}
