package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckInternal;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckProperty;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;

    @Override
    @Nullable
    public Mqtt5SubAckInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() < 3) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int propertiesLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertiesLength < 0) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        while (in.readerIndex() - propertiesStartIndex < propertiesLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5SubAckProperty.REASON_STRING:
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
                case Mqtt5SubAckProperty.USER_PROPERTY:
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

        final int reasonCodeCount = in.readableBytes();
        final ImmutableList.Builder<Mqtt5SubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builderWithExpectedSize(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5SubAckReasonCode reasonCode = Mqtt5SubAckReasonCode.fromCode(in.readByte());
            if (reasonCode == null) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;
        if (userPropertiesBuilder != null) {
            userProperties = userPropertiesBuilder.build();
        }

        final Mqtt5SubAckImpl subAck = new Mqtt5SubAckImpl(reasonCodes, reasonString, userProperties);

        final Mqtt5SubAckInternal subAckInternal = new Mqtt5SubAckInternal(subAck);
        subAckInternal.setPacketIdentifier(packetIdentifier);

        return subAckInternal;
    }

}
