package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRecDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;

    @Override
    @Nullable
    public Mqtt5PubRecInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() < 2) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubRecReasonCode reasonCode = DEFAULT_REASON_CODE;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubRecReasonCode.fromCode(in.readUnsignedByte());
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
                if (in.readableBytes() != propertiesLength) {
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (propertyIdentifier < 0) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case Mqtt5PubRecProperty.REASON_STRING:
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
                        case Mqtt5PubRecProperty.USER_PROPERTY:
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

        final Mqtt5PubRecImpl pubRec = new Mqtt5PubRecImpl(reasonCode, reasonString, userProperties);

        final Mqtt5PubRecInternal pubAckInternal = new Mqtt5PubRecInternal(pubRec);
        pubAckInternal.setPacketIdentifier(packetIdentifier);

        return pubAckInternal;
    }

}
