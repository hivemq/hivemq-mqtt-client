package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRecDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Override
    @Nullable
    public Mqtt5PubRecInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("PUBREC", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubRecReasonCode reasonCode = DEFAULT_REASON_CODE;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubRecReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("PUBREC", channel);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("PUBREC", channel);
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        disconnectMalformedPropertyIdentifier(channel);
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5PubRecProperty.REASON_STRING:
                            reasonString =
                                    decodeReasonStringCheckProblemInformationRequested(reasonString, channel, in);
                            if (reasonString == null) {
                                return null;
                            }
                            break;

                        case Mqtt5PubRecProperty.USER_PROPERTY:
                            userPropertiesBuilder =
                                    decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder, channel,
                                            in);
                            if (userPropertiesBuilder == null) {
                                return null;
                            }
                            break;

                        default:
                            disconnectWrongProperty("PUBREC", channel);
                            return null;
                    }
                }
            }
        }

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.build(userPropertiesBuilder);

        final Mqtt5PubRecImpl pubRec = new Mqtt5PubRecImpl(reasonCode, reasonString, userProperties);

        return new Mqtt5PubRecInternal(pubRec, packetIdentifier);
    }

}
