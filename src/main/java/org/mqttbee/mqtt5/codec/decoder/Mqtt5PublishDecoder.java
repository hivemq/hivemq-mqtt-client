package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.DEFAULT_NO_TOPIC_ALIAS;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal.NO_PACKET_IDENTIFIER_QOS_0;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishDecoder implements Mqtt5MessageDecoder {

    @Override
    @Nullable
    public Mqtt5PublishInternal decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
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

        int packetIdentifier = NO_PACKET_IDENTIFIER_QOS_0;
        if (qos == Mqtt5QoS.AT_MOST_ONCE) {
            if (in.readableBytes() < 2) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }
            packetIdentifier = in.readUnsignedShort();
        }

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

        long messageExpiryInterval = DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY;
        Mqtt5PayloadFormatIndicator payloadFormatIndicator = null;
        Mqtt5UTF8String contentType = null;
        Mqtt5UTF8String responseTopic = null;
        byte[] correlationData = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;
        int topicAlias = DEFAULT_NO_TOPIC_ALIAS;
        ImmutableIntArray.Builder subscriptionIdentifiersBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        while (in.readerIndex() - propertiesStartIndex < propertiesLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5PublishProperty.MESSAGE_EXPIRY_INTERVAL:
                    if (messageExpiryInterval != DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
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
                case Mqtt5PublishProperty.PAYLOAD_FORMAT_INDICATOR:
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
                case Mqtt5PublishProperty.CONTENT_TYPE:
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
                case Mqtt5PublishProperty.RESPONSE_TOPIC:
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
                case Mqtt5PublishProperty.CORRELATION_DATA:
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
                case Mqtt5PublishProperty.USER_PROPERTY:
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
                case Mqtt5PublishProperty.TOPIC_ALIAS:
                    if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 2) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    topicAlias = in.readUnsignedShort();
                    break;
                case Mqtt5PublishProperty.SUBSCRIPTION_IDENTIFIER:
                    if (subscriptionIdentifiersBuilder == null) {
                        subscriptionIdentifiersBuilder = ImmutableIntArray.builder();
                    }
                    final int subscriptionIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (subscriptionIdentifier < 0) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    subscriptionIdentifiersBuilder.add(subscriptionIdentifier);
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

        ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;
        if (userPropertiesBuilder != null) {
            userProperties = userPropertiesBuilder.build();
        }

        final Mqtt5PublishImpl publish = new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval,
                payloadFormatIndicator, contentType, responseTopic, correlationData, userProperties);

        final Mqtt5PublishInternal publishInternal = new Mqtt5PublishInternal(publish);
        publishInternal.setDup(dup);
        publishInternal.setPacketIdentifier(packetIdentifier);
        if (subscriptionIdentifiersBuilder != null) {
            publishInternal.setSubscriptionIdentifiers(subscriptionIdentifiersBuilder.build());
        }

        return publishInternal;
    }

}
