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
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal.*;

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
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong QoS", channel, in);
            return null;
        }

        final Mqtt5Topic topic = Mqtt5Topic.from(in);
        if (topic == null) {
            disconnect(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed topic", channel, in);
            return null;
        }

        int packetIdentifier = NO_PACKET_IDENTIFIER_QOS_0;
        if (qos != Mqtt5QoS.AT_MOST_ONCE) {
            if (in.readableBytes() < 2) {
                disconnectRemainingLengthTooShort(channel, in);
                return null;
            }
            packetIdentifier = in.readUnsignedShort();
        }

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel, in);
            return null;
        }
        if (in.readableBytes() < propertyLength) {
            disconnectRemainingLengthTooShort(channel, in);
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
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel, in);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5PublishProperty.MESSAGE_EXPIRY_INTERVAL:
                    if (!checkIntOnlyOnce(
                            messageExpiryInterval, DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY, "message expiry interval",
                            channel, in)) {
                        return null;
                    }
                    messageExpiryInterval = in.readUnsignedInt();
                    break;

                case Mqtt5PublishProperty.PAYLOAD_FORMAT_INDICATOR:
                    if (!checkByteOnlyOnce(payloadFormatIndicator != null, "payload format indicator",
                            channel, in)) {
                        return null;
                    }
                    payloadFormatIndicator = Mqtt5PayloadFormatIndicator.fromCode(in.readUnsignedByte());
                    if (payloadFormatIndicator == null) {
                        disconnect(
                                Mqtt5DisconnectReasonCode.MALFORMED_PACKET, " wrong payload format indicator", channel,
                                in);
                        return null;
                    }
                    break;

                case Mqtt5PublishProperty.CONTENT_TYPE:
                    contentType = decodeUTF8StringOnlyOnce(contentType, "content type", channel, in);
                    if (contentType == null) {
                        return null;
                    }
                    break;

                case Mqtt5PublishProperty.RESPONSE_TOPIC:
                    responseTopic = decodeUTF8StringOnlyOnce(responseTopic, "response topic", channel, in);
                    if (responseTopic == null) {
                        return null;
                    }
                    break;

                case Mqtt5PublishProperty.CORRELATION_DATA:
                    correlationData = decodeBinaryDataOnlyOnce(correlationData, "correlation data", channel, in);
                    if (correlationData == null) {
                        return null;
                    }
                    break;

                case Mqtt5PublishProperty.USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                case Mqtt5PublishProperty.TOPIC_ALIAS:
                    if (!checkShortOnlyOnce(topicAlias, DEFAULT_NO_TOPIC_ALIAS, "topic alias", channel, in)) {
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
                        disconnect(
                                Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed subscription identifier",
                                channel, in);
                        return null;
                    }
                    subscriptionIdentifiersBuilder.add(subscriptionIdentifier);
                    break;

                default:
                    disconnectWrongProperty("PUBLISH", channel, in);
                    return null;
            }
        }

        if (readPropertyLength != propertyLength) {
            disconnectMalformedPropertyLength(channel, in);
            return null;
        }

        final int payloadLength = in.readableBytes();
        byte[] payload = null;
        if (payloadLength > 0) {
            payload = new byte[payloadLength];
            in.readBytes(payload);
        }

        final ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.build(userPropertiesBuilder);

        final Mqtt5PublishImpl publish = new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval,
                payloadFormatIndicator, contentType, responseTopic, correlationData, userProperties);

        final ImmutableIntArray subscriptionIdentifiers =
                (subscriptionIdentifiersBuilder == null) ? DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS :
                        subscriptionIdentifiersBuilder.build();

        return new Mqtt5PublishInternal(publish, packetIdentifier, dup, topicAlias, subscriptionIdentifiers);
    }

}
