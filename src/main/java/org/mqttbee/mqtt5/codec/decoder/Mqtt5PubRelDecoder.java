package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ClientData;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelProperty;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0010;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Override
    @Nullable
    public Mqtt5PubRelImpl decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final Mqtt5ClientData clientData) {
        final Channel channel = clientData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("PUBREL", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubRelReasonCode reasonCode = DEFAULT_REASON_CODE;
        Mqtt5UTF8StringImpl reasonString = null;
        ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubRelReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("PUBREL", channel);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("PUBREL", channel);
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        disconnectMalformedPropertyIdentifier(channel);
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5PubRelProperty.REASON_STRING:
                            reasonString =
                                    decodeReasonStringCheckProblemInformationRequested(reasonString, clientData, in);
                            if (reasonString == null) {
                                return null;
                            }
                            break;

                        case Mqtt5PubRelProperty.USER_PROPERTY:
                            userPropertiesBuilder =
                                    decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder,
                                            clientData, in);
                            if (userPropertiesBuilder == null) {
                                return null;
                            }
                            break;

                        default:
                            disconnectWrongProperty("PUBREL", channel);
                            return null;
                    }
                }
            }
        }

        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(userPropertiesBuilder);

        return new Mqtt5PubRelImpl(packetIdentifier, reasonCode, reasonString, userProperties);
    }

}
