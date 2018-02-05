package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
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
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(publishInternal, out, maximumPacketSize);
        encodeVariableHeader(publishInternal, out, maximumPacketSize);
        encodePayload(publishInternal, out);
    }

    public int encodedRemainingLengthWithoutProperties(@NotNull final Mqtt5PublishImpl publish) {
        int remainingLength = 0;

        remainingLength += publish.getTopic().encodedLength();

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final byte[] payload = publish.getRawPayload();
        if (payload != null) {
            remainingLength += payload.length;
        }

        return remainingLength;
    }

    public int additionalRemainingLength(@NotNull final Mqtt5PublishInternal publishInternal) {
        int additionalRemainingLength = 0;

        if ((publishInternal.getTopicAlias() != DEFAULT_NO_TOPIC_ALIAS) && !publishInternal.isNewTopicAlias()) {
            additionalRemainingLength = 2 - publishInternal.getWrapped().getTopic().encodedLength();
        }

        return additionalRemainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PublishImpl publish) {
        int propertyLength = 0;

        propertyLength +=
                intPropertyEncodedLength(publish.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY);
        propertyLength += propertyEncodedLength(publish.getRawPayloadFormatIndicator());
        propertyLength += nullablePropertyEncodedLength(publish.getRawContentType());
        propertyLength += nullablePropertyEncodedLength(publish.getRawResponseTopic());
        propertyLength += nullablePropertyEncodedLength(publish.getRawCorrelationData());
        propertyLength += publish.getUserProperties().encodedLength();

        return propertyLength;
    }

    public int additionalPropertyLength(@NotNull final Mqtt5PublishInternal publishInternal) {
        int additionalPropertyLength = 0;

        additionalPropertyLength += shortPropertyEncodedLength(publishInternal.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS);

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            additionalPropertyLength += variableByteIntegerPropertyEncodedLength(subscriptionIdentifiers.get(i));
        }

        return additionalPropertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out,
            final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = publishInternal.getWrapped();

        int flags = 0;
        if (publishInternal.isDup()) {
            flags |= 0b1000;
        }
        flags |= publish.getQos().getCode() << 1;
        if (publish.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        Mqtt5DataTypes.encodeVariableByteInteger(publishInternal.encodedRemainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out,
            final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = publishInternal.getWrapped();

        if ((publishInternal.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (publishInternal.isNewTopicAlias())) {
            publish.getTopic().to(out);
        } else {
            out.writeShort(0);
        }

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            out.writeShort(publishInternal.getPacketIdentifier());
        }

        encodeProperties(publishInternal, out, maximumPacketSize);
    }

    private void encodeProperties(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out,
            final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = publishInternal.getWrapped();

        final int propertyLength = publishInternal.encodedPropertyLength(maximumPacketSize);
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeIntProperty(
                MESSAGE_EXPIRY_INTERVAL, publish.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, publish.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, publish.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, publish.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, publish.getRawCorrelationData(), out);
        publishInternal.encodeUserProperties(maximumPacketSize, out);

        encodeShortProperty(TOPIC_ALIAS, publishInternal.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
        }
    }

    private void encodePayload(
            @NotNull final Mqtt5PublishInternal publishInternal, @NotNull final ByteBuf out) {

        final byte[] payload = publishInternal.getWrapped().getRawPayload();
        if (payload != null) {
            out.writeBytes(payload);
        }
    }

}
