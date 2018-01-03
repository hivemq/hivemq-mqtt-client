package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtils.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 5; // reason code (1) + property length (min 1) + auth method (1 + min 2)

    @Override
    @Nullable
    public Mqtt5AuthImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("AUTH", channel, in);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel, in);
            return null;
        }

        final Mqtt5AuthReasonCode reasonCode = Mqtt5AuthReasonCode.fromCode(in.readByte());
        if (reasonCode == null) {
            disconnectWrongReasonCode("AUTH", channel, in);
            return null;
        }

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel, in);
            return null;
        }
        if (in.readableBytes() != propertyLength) {
            disconnectMustNotHavePayload("AUTH", channel, in);
            return null;
        }

        Mqtt5UTF8String method = null;
        byte[] data = null;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        while (in.isReadable()) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel, in);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5AuthProperty.AUTHENTICATION_METHOD:
                    method = decodeUTF8StringOnlyOnce(method, "authentication method", channel, in);
                    if (method == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.AUTHENTICATION_DATA:
                    data = decodeBinaryDataOnlyOnce(data, "authentication data", channel, in);
                    if (data == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.REASON_STRING:
                    reasonString = decodeUTF8StringOnlyOnce(reasonString, "reason string", channel, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("AUTH", channel, in);
                    return null;
            }
        }

        if (method == null) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "AUTH must not omit authentication method", channel, in);
            return null;
        }

        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(userPropertiesBuilder);

        return new Mqtt5AuthImpl(reasonCode, method, data, reasonString, userProperties);
    }

}
