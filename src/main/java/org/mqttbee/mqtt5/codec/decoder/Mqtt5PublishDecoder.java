package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PublishEncoder;
import org.mqttbee.mqtt5.handler.Mqtt5ClientData;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishDecoder implements Mqtt5MessageDecoder {

    private static final int MIN_REMAINING_LENGTH = 3; // topic name (min 2) + property length (min 1)

    @Override
    @Nullable
    public Mqtt5PublishWrapper decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final Mqtt5ClientData clientData) {
        final Channel channel = clientData.getChannel();

        final boolean dup = (flags & 0b1000) != 0;
        final boolean retain = (flags & 0b0001) != 0;

        final Mqtt5QoS qos = Mqtt5QoS.fromCode((flags & 0b0110) >> 1);
        if (qos == null) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong QoS", channel);
            return null;
        }
        if ((qos == Mqtt5QoS.AT_MOST_ONCE) && dup) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "DUP flag must be 0 if QoS is 0", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final byte[] topicBinary = Mqtt5DataTypes.decodeBinaryData(in);
        if (topicBinary == null) {
            disconnect(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed topic", channel);
            return null;
        }
        Mqtt5TopicImpl topic = null;
        if (topicBinary.length != 0) {
            topic = Mqtt5TopicImpl.from(topicBinary);
            if (topic == null) {
                disconnect(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed topic", channel);
                return null;
            }
        }

        int packetIdentifier = NO_PACKET_IDENTIFIER_QOS_0;
        if (qos != Mqtt5QoS.AT_MOST_ONCE) {
            if (in.readableBytes() < 2) {
                disconnectRemainingLengthTooShort(channel);
                return null;
            }
            packetIdentifier = in.readUnsignedShort();
        }

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }
        if (in.readableBytes() < propertyLength) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
        Mqtt5PayloadFormatIndicator payloadFormatIndicator = null;
        Mqtt5UTF8StringImpl contentType = null;
        Mqtt5TopicImpl responseTopic = null;
        byte[] correlationData = null;
        ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder = null;
        int topicAlias = DEFAULT_NO_TOPIC_ALIAS;
        TopicAliasUsage topicAliasUsage = TopicAliasUsage.HAS_NOT;
        ImmutableIntArray.Builder subscriptionIdentifiersBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5PublishProperty.MESSAGE_EXPIRY_INTERVAL:
                    if (!checkIntOnlyOnce(messageExpiryInterval, MESSAGE_EXPIRY_INTERVAL_INFINITY,
                            "message expiry interval", channel, in)) {
                        return null;
                    }
                    messageExpiryInterval = in.readUnsignedInt();
                    break;

                case Mqtt5PublishProperty.PAYLOAD_FORMAT_INDICATOR:
                    if (!checkByteOnlyOnce(payloadFormatIndicator != null, "payload format indicator", channel, in)) {
                        return null;
                    }
                    payloadFormatIndicator = Mqtt5PayloadFormatIndicator.fromCode(in.readUnsignedByte());
                    if (payloadFormatIndicator == null) {
                        disconnect(
                                Mqtt5DisconnectReasonCode.MALFORMED_PACKET, " wrong payload format indicator", channel);
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
                    if (responseTopic != null) {
                        disconnectOnlyOnce("response topic", channel);
                        return null;
                    }
                    responseTopic = Mqtt5TopicImpl.from(in);
                    if (responseTopic == null) {
                        disconnect(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed response topic", channel);
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
                    if (topicAlias == 0) {
                        disconnect(Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID, "topic alias must not be 0", channel);
                        return null;
                    }
                    topicAliasUsage = TopicAliasUsage.HAS;
                    break;

                case Mqtt5PublishProperty.SUBSCRIPTION_IDENTIFIER:
                    if (subscriptionIdentifiersBuilder == null) {
                        subscriptionIdentifiersBuilder = ImmutableIntArray.builder();
                    }
                    final int subscriptionIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
                    if (subscriptionIdentifier < 0) {
                        disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed subscription identifier",
                                channel);
                        return null;
                    }
                    if (subscriptionIdentifier == 0) {
                        disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "subscription identifier must not be 0",
                                channel);
                        return null;
                    }
                    subscriptionIdentifiersBuilder.add(subscriptionIdentifier);
                    break;

                default:
                    disconnectWrongProperty("PUBLISH", channel);
                    return null;
            }
        }

        if (readPropertyLength != propertyLength) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }

        boolean isNewTopicAlias = false;
        if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
            final Mqtt5TopicImpl[] topicAliasMapping = Mqtt5ClientData.get(channel).getTopicAliasMapping();
            if ((topicAliasMapping == null) || (topicAlias > topicAliasMapping.length)) {
                disconnect(
                        Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID,
                        "topic alias must not exceed topic alias maximum", channel);
                return null;
            }
            if (topic == null) {
                topic = topicAliasMapping[topicAlias - 1];
                if (topic == null) {
                    disconnect(Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID, "topic alias has no mapping", channel);
                    return null;
                }
            } else {
                topicAliasMapping[topicAlias - 1] = topic;
                isNewTopicAlias = true;
            }
        } else if (topic == null) {
            disconnect(
                    Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID,
                    "topic alias must be present if topic name is zero length", channel);
            return null;
        }

        final int payloadLength = in.readableBytes();
        byte[] payload = null;
        if (payloadLength > 0) {
            payload = new byte[payloadLength];
            in.readBytes(payload);

            if (payloadFormatIndicator == Mqtt5PayloadFormatIndicator.UTF_8) {
                final Boolean validatePayloadFormat = channel.attr(ChannelAttributes.VALIDATE_PAYLOAD_FORMAT).get();
                if ((validatePayloadFormat != null) && validatePayloadFormat) {
                    if (!Utf8.isWellFormed(payload)) {
                        disconnect(Mqtt5DisconnectReasonCode.PAYLOAD_FORMAT_INVALID, "payload is not valid UTF-8",
                                channel);
                        return null;
                    }
                }
            }
        }

        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(userPropertiesBuilder);

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                        contentType, responseTopic, correlationData, topicAliasUsage, userProperties,
                        Mqtt5PublishEncoder.PROVIDER);

        final ImmutableIntArray subscriptionIdentifiers =
                (subscriptionIdentifiersBuilder == null) ? DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS :
                        subscriptionIdentifiersBuilder.build();

        return publish.wrap(packetIdentifier, dup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
    }

}
