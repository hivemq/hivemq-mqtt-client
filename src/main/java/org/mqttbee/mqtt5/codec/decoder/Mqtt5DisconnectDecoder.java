package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import javax.inject.Singleton;

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
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        Mqtt5DisconnectReasonCode reasonCode = DEFAULT_REASON_CODE;
        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        Mqtt5UTF8String serverReference = null;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5DisconnectReasonCode.fromCode(in.readByte());
            if (reasonCode == null) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            if (in.isReadable()) {
                final int propertiesLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
                if (propertiesLength < 0) {
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
                }
                if (in.readableBytes() < propertiesLength) {
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
                }

                final int propertiesStartIndex = in.readerIndex();
                while (in.readerIndex() - propertiesStartIndex < propertiesLength) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL:
                            if (sessionExpiryInterval != SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
                                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                                in.clear();
                                return null;
                            }
                            if (in.readableBytes() < 4) {
                                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                                in.clear();
                                return null;
                            }
                            sessionExpiryInterval = in.readUnsignedInt();
                            break;
                        case Mqtt5DisconnectProperty.SERVER_REFERENCE:
                            if (serverReference != null) {
                                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                                in.clear();
                                return null;
                            }
                            serverReference = Mqtt5UTF8String.from(in);
                            if (serverReference == null) {
                                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                                in.clear();
                                return null;
                            }
                            break;
                        case Mqtt5DisconnectProperty.REASON_STRING:
                            if (reasonString != null) {
                                // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                                in.clear();
                                return null;
                            }
                            reasonString = Mqtt5UTF8String.from(in);
                            if (reasonString == null) {
                                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                                in.clear();
                                return null;
                            }
                            break;
                        case Mqtt5DisconnectProperty.USER_PROPERTY:
                            if (userPropertiesBuilder == null) {
                                userPropertiesBuilder = ImmutableList.builder();
                            }
                            final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(in);
                            if (userProperty == null) {
                                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                                in.clear();
                                return null;
                            }
                            userPropertiesBuilder.add(userProperty);
                            break;
                        default:
                            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                            in.clear();
                            return null;
                    }
                }
            }
        }

        ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;
        if (userPropertiesBuilder != null) {
            userProperties = userPropertiesBuilder.build();
        }

        return new Mqtt5DisconnectImpl(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties);
    }

}
