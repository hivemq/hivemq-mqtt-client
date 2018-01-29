package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty.*;

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

        if ((publishInternal.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (publishInternal.isNewTopicAlias())) {
            remainingLength += publish.getTopic().encodedLength();
        } else {
            remainingLength += 2;
        }

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

        if (publish.getRawMessageExpiryInterval() != MESSAGE_EXPIRY_INTERVAL_INFINITY) {
            propertyLength += 5;
        }

        if (publish.getRawPayloadFormatIndicator() != null) {
            propertyLength += 2;
        }

        final Mqtt5UTF8String contentType = publish.getRawContentType();
        if (contentType != null) {
            propertyLength += 1 + contentType.encodedLength();
        }

        final Mqtt5Topic responseTopic = publish.getRawResponseTopic();
        if (responseTopic != null) {
            propertyLength += 1 + responseTopic.encodedLength();
        }

        final byte[] correlationData = publish.getRawCorrelationData();
        if (correlationData != null) {
            if (!Mqtt5DataTypes.isInBinaryDataRange(correlationData)) {
                throw new Mqtt5BinaryDataExceededException("correlation data");
            }
            propertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
        }

        propertyLength += publish.getRawUserProperties().encodedLength();

        if (publishInternal.getTopicAlias() != DEFAULT_NO_TOPIC_ALIAS) {
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

        if ((publishInternal.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (publishInternal.isNewTopicAlias())) {
            publish.getTopic().to(out);
        } else {
            out.writeShort(0);
        }

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            out.writeShort(publishInternal.getPacketIdentifier());
        }

        encodeProperties(publishInternal, out);
    }

    private void encodeProperties(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        final int propertyLength = publishInternal.encodedPropertyLength();
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeIntProperty(
                MESSAGE_EXPIRY_INTERVAL, publish.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodePropertyNullable(PAYLOAD_FORMAT_INDICATOR, publish.getRawPayloadFormatIndicator(), out);
        encodePropertyNullable(CONTENT_TYPE, publish.getRawContentType(), out);
        encodePropertyNullable(RESPONSE_TOPIC, publish.getRawResponseTopic(), out);
        encodePropertyNullable(CORRELATION_DATA, publish.getRawCorrelationData(), out);
        publish.getRawUserProperties().encode(out);

        encodeShortProperty(TOPIC_ALIAS, DEFAULT_NO_TOPIC_ALIAS, publishInternal.getTopicAlias(), out);

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
        }
    }

    private void encodePayload(@NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {
        final byte[] payload = publishInternal.getPublish().getRawPayload();
        if (payload != null) {
            out.writeBytes(payload);
        }
    }

}
