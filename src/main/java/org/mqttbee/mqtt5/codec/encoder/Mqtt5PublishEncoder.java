package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishEncoder implements Mqtt5MessageEncoder<Mqtt5PublishInternal> {

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

    public void encode(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        final int propertiesLength = calculatePropertiesLength(publishInternal);
        final int remainingLength = calculateRemainingLength(publishInternal, propertiesLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(publishInternal, remainingLength, out);
        encodeVariableHeader(publishInternal, propertiesLength, out);
        encodePayload(publishInternal, out);
    }

    private int calculateRemainingLength(
            @NotNull final Mqtt5PublishInternal publishInternal, final int propertiesLength) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        int remainingLength = 0;

        remainingLength += publish.getTopic().encodedLength();

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertiesLength) + propertiesLength;

        final byte[] payload = publish.getRawPayload();
        if (payload != null) {
            remainingLength += payload.length;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertiesLength(@NotNull final Mqtt5PublishInternal publishInternal) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        int propertiesLength = 0;

        if (publish.getMessageExpiryInterval() != Mqtt5Publish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
            propertiesLength += 5;
        }
        if (publish.getRawPayloadFormatIndicator() != null) {
            propertiesLength += 2;
        }
        final Mqtt5UTF8String contentType = publish.getRawContentType();
        if (contentType != null) {
            propertiesLength += 1 + contentType.encodedLength();
        }
        final Mqtt5UTF8String responseTopic = publish.getRawResponseTopic();
        if (responseTopic != null) {
            propertiesLength += 1 + responseTopic.encodedLength();
        }
        final byte[] correlationData = publish.getRawCorrelationData();
        if (correlationData != null) {
            propertiesLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
        }
        propertiesLength += Mqtt5UserProperty.encodedLength(publish.getUserProperties());

        if (publishInternal.getTopicAlias() != Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS) {
            propertiesLength += 3;
        }
        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            propertiesLength += 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(subscriptionIdentifiers.get(i));
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertiesLength)) {
            // TODO exception properties size exceeded
        }

        return propertiesLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5PublishInternal publishInternal, final int remainingLength,
            @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        int flags = 0;
        if (publishInternal.isDup()) {
            flags |= 0b1000;
        }
        flags |= publish.getQos().getCode() << 1;
        if (publish.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5PublishInternal publishInternal, final int propertiesLength,
            @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        publish.getTopic().to(out);

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            out.writeShort(publishInternal.getPacketIdentifier());
        }

        encodeProperties(publishInternal, propertiesLength, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5PublishInternal publishInternal, final int propertiesLength,
            @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        final long messageExpiryInterval = publish.getMessageExpiryInterval();
        if (messageExpiryInterval != Mqtt5Publish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
            out.writeByte(Mqtt5PublishProperty.MESSAGE_EXPIRY_INTERVAL);
            out.writeInt((int) messageExpiryInterval);
        }

        final Mqtt5PayloadFormatIndicator payloadFormatIndicator = publish.getRawPayloadFormatIndicator();
        if (payloadFormatIndicator != null) {
            out.writeByte(Mqtt5PublishProperty.PAYLOAD_FORMAT_INDICATOR);
            out.writeByte(payloadFormatIndicator.getCode());
        }

        final Mqtt5UTF8String contentType = publish.getRawContentType();
        if (contentType != null) {
            out.writeByte(Mqtt5PublishProperty.CONTENT_TYPE);
            contentType.to(out);
        }

        final Mqtt5UTF8String responseTopic = publish.getRawResponseTopic();
        if (responseTopic != null) {
            out.writeByte(Mqtt5PublishProperty.RESPONSE_TOPIC);
            responseTopic.to(out);
        }

        final byte[] correlationData = publish.getRawCorrelationData();
        if (correlationData != null) {
            out.writeByte(Mqtt5PublishProperty.CORRELATION_DATA);
            Mqtt5DataTypes.encodeBinaryData(correlationData, out);
        }

        Mqtt5UserProperty.encode(publish.getUserProperties(), out);

        final int topicAlias = publishInternal.getTopicAlias();
        if (topicAlias != Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS) {
            out.writeByte(Mqtt5PublishProperty.TOPIC_ALIAS);
            out.writeShort(topicAlias);
        }

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            out.writeByte(Mqtt5PublishProperty.SUBSCRIPTION_IDENTIFIER);
            Mqtt5DataTypes.encodeVariableByteInteger(subscriptionIdentifiers.get(i), out);
        }
    }

    private void encodePayload(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
        final byte[] payload = publishInternal.getPublish().getRawPayload();
        if (payload != null) {
            out.writeBytes(payload);
        }
    }

}
