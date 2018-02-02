package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;

    @Override
    @Nullable
    public Mqtt5DisconnectImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("DISCONNECT", channel);
            return null;
        }

        Mqtt5DisconnectReasonCode reasonCode = DEFAULT_REASON_CODE;
        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        Mqtt5UTF8StringImpl serverReference = null;
        Mqtt5UTF8StringImpl reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5DisconnectReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("DISCONNECT", channel);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("DISCONNECT", channel);
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        disconnectMalformedPropertyIdentifier(channel);
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL:
                            if (!checkIntOnlyOnce(sessionExpiryInterval, SESSION_EXPIRY_INTERVAL_FROM_CONNECT,
                                    "session expiry interval", channel, in)) {
                                return null;
                            }
                            sessionExpiryInterval = in.readUnsignedInt();
                            break;

                        case Mqtt5DisconnectProperty.SERVER_REFERENCE:
                            serverReference =
                                    decodeUTF8StringOnlyOnce(serverReference, "server reference", channel, in);
                            if (serverReference == null) {
                                return null;
                            }
                            break;

                        case Mqtt5DisconnectProperty.REASON_STRING:
                            reasonString = decodeUTF8StringOnlyOnce(reasonString, "reason string", channel, in);
                            if (reasonString == null) {
                                return null;
                            }
                            break;

                        case Mqtt5DisconnectProperty.USER_PROPERTY:
                            userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                            if (userPropertiesBuilder == null) {
                                return null;
                            }
                            break;

                        default:
                            disconnectWrongProperty("DISCONNECT", channel);
                            return null;
                    }
                }
            }
        }

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.build(userPropertiesBuilder);

        return new Mqtt5DisconnectImpl(
                reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties);
    }

}
