package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt5.message.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Override
    @Nullable
    public Mqtt5SubAckImpl decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final Mqtt5ClientDataImpl clientData) {
        final Channel channel = clientData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("SUBACK", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }
        if (in.readableBytes() < propertyLength) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        Mqtt5UTF8StringImpl reasonString = null;
        ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5SubAckProperty.REASON_STRING:
                    reasonString = decodeReasonStringCheckProblemInformationRequested(reasonString, clientData, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case Mqtt5SubAckProperty.USER_PROPERTY:
                    userPropertiesBuilder =
                            decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder, clientData, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("SUBACK", channel);
                    return null;
            }
        }

        if (readPropertyLength != propertyLength) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }

        final int reasonCodeCount = in.readableBytes();
        if (reasonCodeCount == 0) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "SUBACK must contain at least one reason code", channel);
            return null;
        }

        final ImmutableList.Builder<Mqtt5SubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builderWithExpectedSize(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5SubAckReasonCode reasonCode = Mqtt5SubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("SUBACK", channel);
                return null;
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(userPropertiesBuilder);

        return new Mqtt5SubAckImpl(packetIdentifier, reasonCodes, reasonString, userProperties);
    }

}
