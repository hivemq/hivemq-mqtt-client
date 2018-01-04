package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
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

    public static final Mqtt5PublishEncoder INSTANCE = new Mqtt5PublishEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

    @Override
    public void encode(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(publishInternal, out);
        encodeVariableHeader(publishInternal, out);
        encodePayload(publishInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PublishInternal publishInternal) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        int remainingLength = 0;

        remainingLength += publish.getTopic().encodedLength();

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final int propertyLength = publishInternal.encodedPropertyLength();
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        final byte[] payload = publish.getRawPayload();
        if (payload != null) {
            remainingLength += payload.length;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PublishInternal publishInternal) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        int propertyLength = 0;

        if (publish.getMessageExpiryInterval() != Mqtt5Publish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
            propertyLength += 5;
        }

        if (publish.getRawPayloadFormatIndicator() != null) {
            propertyLength += 2;
        }

        final Mqtt5UTF8String contentType = publish.getRawContentType();
        if (contentType != null) {
            propertyLength += 1 + contentType.encodedLength();
        }

        final Mqtt5UTF8String responseTopic = publish.getRawResponseTopic();
        if (responseTopic != null) {
            propertyLength += 1 + responseTopic.encodedLength();
        }

        final byte[] correlationData = publish.getRawCorrelationData();
        if (correlationData != null) {
            propertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
        }

        propertyLength += Mqtt5UserProperty.encodedLength(publish.getUserProperties());

        if (publishInternal.getTopicAlias() != Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS) {
            propertyLength += 3;
        }

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            propertyLength += 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(subscriptionIdentifiers.get(i));
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
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

        final int remainingLength = publishInternal.encodedRemainingLength();
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        publish.getTopic().to(out);

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            out.writeShort(publishInternal.getPacketIdentifier());
        }

        encodeProperties(publishInternal, out);
    }

    private void encodeProperties(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        final int propertyLength = publishInternal.encodedPropertyLength();
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

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
