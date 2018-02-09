package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5MessageWrapperEncoder;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishEncoder extends Mqtt5MessageWrapperEncoder<Mqtt5PublishInternal> {

    public static final Function<Mqtt5PublishInternal, Mqtt5PublishEncoder> PROVIDER = Mqtt5PublishEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

    private Mqtt5PublishEncoder(@NotNull final Mqtt5PublishInternal wrapper) {
        super(wrapper);
    }

    @Override
    public int additionalRemainingLength() {
        int additionalRemainingLength = 0;

        if ((message.getTopicAlias() != DEFAULT_NO_TOPIC_ALIAS) && !message.isNewTopicAlias()) {
            additionalRemainingLength = 2 - message.getWrapped().getTopic().encodedLength();
        }

        return additionalRemainingLength;
    }

    @Override
    public int additionalPropertyLength() {
        int additionalPropertyLength = 0;

        additionalPropertyLength += shortPropertyEncodedLength(message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            additionalPropertyLength += variableByteIntegerPropertyEncodedLength(subscriptionIdentifiers.get(i));
        }

        return additionalPropertyLength;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(out, maximumPacketSize);
        encodeVariableHeader(out, maximumPacketSize);
        encodePayload(out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = message.getWrapped();

        int flags = 0;
        if (message.isDup()) {
            flags |= 0b1000;
        }
        flags |= publish.getQos().getCode() << 1;
        if (publish.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        Mqtt5DataTypes.encodeVariableByteInteger(encodedRemainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = message.getWrapped();

        if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (message.isNewTopicAlias())) {
            publish.getTopic().to(out);
        } else {
            out.writeShort(0);
        }

        if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            out.writeShort(message.getPacketIdentifier());
        }

        encodeProperties(out, maximumPacketSize);
    }

    private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
        final Mqtt5PublishImpl publish = message.getWrapped();

        final int propertyLength = encodedPropertyLength(maximumPacketSize);
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeIntProperty(
                MESSAGE_EXPIRY_INTERVAL, publish.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, publish.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, publish.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, publish.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, publish.getRawCorrelationData(), out);
        encodeUserProperties(maximumPacketSize, out);

        encodeShortProperty(TOPIC_ALIAS, message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
        }
    }

    private void encodePayload(@NotNull final ByteBuf out) {
        final byte[] payload = message.getWrapped().getRawPayload();
        if (payload != null) {
            out.writeBytes(payload);
        }
    }


    public static class Mqtt5WrappedPublishEncoder
            extends Mqtt5WrappedMessageEncoder<Mqtt5PublishImpl, Mqtt5PublishInternal> {

        public static final Function<Mqtt5PublishImpl, Mqtt5WrappedPublishEncoder> PROVIDER =
                Mqtt5WrappedPublishEncoder::new;

        private Mqtt5WrappedPublishEncoder(@NotNull final Mqtt5PublishImpl message) {
            super(message);
        }

        @Override
        protected int calculateEncodedRemainingLengthWithoutProperties() {
            int remainingLength = 0;

            remainingLength += message.getTopic().encodedLength();

            if (message.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
                remainingLength += 2;
            }

            final byte[] payload = message.getRawPayload();
            if (payload != null) {
                remainingLength += payload.length;
            }

            return remainingLength;
        }

        @Override
        protected int calculateEncodedPropertyLength() {
            int propertyLength = 0;

            propertyLength +=
                    intPropertyEncodedLength(message.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY);
            propertyLength += propertyEncodedLength(message.getRawPayloadFormatIndicator());
            propertyLength += nullablePropertyEncodedLength(message.getRawContentType());
            propertyLength += nullablePropertyEncodedLength(message.getRawResponseTopic());
            propertyLength += nullablePropertyEncodedLength(message.getRawCorrelationData());
            propertyLength += message.getUserProperties().encodedLength();

            return propertyLength;
        }

        @Override
        public Function<Mqtt5PublishInternal, Mqtt5PublishEncoder> wrap() {
            return Mqtt5PublishEncoder.PROVIDER;
        }

    }

}
