package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishDecoder implements Mqtt5MessageDecoder {

    @Override
    @Nullable
    public Mqtt5PublishImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        final boolean dup = (flags & 0b1000) != 0;
        final boolean retain = (flags & 0b0001) != 0;

        final Mqtt5QoS qos = Mqtt5QoS.fromCode((flags & 0b0110) >> 1);
        if (qos == null) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final Mqtt5Topic topic = Mqtt5Topic.from(in);
        if (topic == null) {
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

        long messageExpiryInterval = Mqtt5Publish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY;
        Mqtt5PayloadFormatIndicator payloadFormatIndicator = null;
        Mqtt5UTF8String contentType = null;
        Mqtt5UTF8String responseTopic = null;
        byte[] correlationData = null;
        List<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;

        final int propertiesStartIndex = in.readerIndex();
        while (in.readerIndex() - propertiesStartIndex < propertiesLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            switch (propertyIdentifier) {
                case MESSAGE_EXPIRY_INTERVAL:
                    if (messageExpiryInterval != Mqtt5Publish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 4) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    messageExpiryInterval = in.readUnsignedInt();
                    break;
                case PAYLOAD_FORMAT_INDICATOR:
                    if (payloadFormatIndicator != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    payloadFormatIndicator = Mqtt5PayloadFormatIndicator.fromCode(in.readByte());
                    if (payloadFormatIndicator == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case CONTENT_TYPE:
                    if (contentType != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    contentType = Mqtt5UTF8String.from(in);
                    if (contentType == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case RESPONSE_TOPIC:
                    if (responseTopic != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    responseTopic = Mqtt5UTF8String.from(in);
                    if (responseTopic == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case CORRELATION_DATA:
                    if (correlationData != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    correlationData = Mqtt5DataTypes.decodeBinaryData(in);
                    if (correlationData == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case USER_PROPERTY:
                    if (userProperties == Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES) {
                        userProperties = new LinkedList<>();
                    }
                    final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(in);
                    if (userProperty == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    userProperties.add(userProperty);
                    break;
                default:
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
            }
        }

        final int payloadLength = in.readableBytes();
        byte[] payload = null;
        if (payloadLength > 0) {
            payload = new byte[payloadLength];
            in.readBytes(payload);
        }

        // TODO packet identifier and dup flag, maybe internal publish impl/wrapper?
        return new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties);
    }

}
