package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper;

import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishProperty.*;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishEncoder extends Mqtt5WrappedMessageEncoder<Mqtt5PublishImpl, Mqtt5PublishWrapper> {

    public static final Function<Mqtt5PublishImpl, Mqtt5PublishEncoder> PROVIDER = Mqtt5PublishEncoder::new;

    private Mqtt5PublishEncoder(@NotNull final Mqtt5PublishImpl message) {
        super(message);
    }

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = 0;

        remainingLength += message.getTopic().encodedLength();

        if (message.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = message.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength +=
                intPropertyEncodedLength(message.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY);
        propertyLength += nullablePropertyEncodedLength(message.getRawPayloadFormatIndicator());
        propertyLength += nullablePropertyEncodedLength(message.getRawContentType());
        propertyLength += nullablePropertyEncodedLength(message.getRawResponseTopic());
        propertyLength += nullablePropertyEncodedLength(message.getRawCorrelationData());
        propertyLength += message.getUserProperties().encodedLength();

        return propertyLength;
    }

    @Override
    void encodeFixedProperties(@NotNull final ByteBuf out) {
        encodeIntProperty(MESSAGE_EXPIRY_INTERVAL, message.getRawMessageExpiryInterval(),
                MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, message.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, message.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, message.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, message.getRawCorrelationData(), out);
    }

    @Override
    public Function<Mqtt5PublishWrapper, Mqtt5PublishWrapperEncoder> wrap() {
        return Mqtt5PublishWrapperEncoder.PROVIDER;
    }


    public static class Mqtt5PublishWrapperEncoder
            extends Mqtt5MessageWrapperEncoder<Mqtt5PublishWrapper, Mqtt5PublishImpl> {

        static final Function<Mqtt5PublishWrapper, Mqtt5PublishWrapperEncoder> PROVIDER =
                Mqtt5PublishWrapperEncoder::new;

        private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

        private Mqtt5PublishWrapperEncoder(@NotNull final Mqtt5PublishWrapper wrapper) {
            super(wrapper);
        }

        @Override
        int additionalRemainingLength() {
            int additionalRemainingLength = 0;

            if ((message.getTopicAlias() != DEFAULT_NO_TOPIC_ALIAS) && !message.isNewTopicAlias()) {
                additionalRemainingLength =
                        Mqtt5DataTypes.EMPTY_BINARY_DATA_LENGTH - message.getWrapped().getTopic().encodedLength();
            }

            return additionalRemainingLength;
        }

        @Override
        int additionalPropertyLength() {
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
            final int maximumPacketSize = Mqtt5ServerDataImpl.get(channel).getMaximumPacketSize();

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

            Mqtt5DataTypes.encodeVariableByteInteger(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            final Mqtt5PublishImpl publish = message.getWrapped();

            if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || (message.isNewTopicAlias())) {
                publish.getTopic().to(out);
            } else {
                Mqtt5DataTypes.encodeEmptyBinaryData(out);
            }

            if (publish.getQos() != Mqtt5QoS.AT_MOST_ONCE) {
                out.writeShort(message.getPacketIdentifier());
            }

            encodeProperties(out, maximumPacketSize);
        }

        private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
            final int propertyLength = propertyLength(maximumPacketSize);
            Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

            message.getWrapped().getEncoder().encodeFixedProperties(out);
            encodeOmissibleProperties(maximumPacketSize, out);

            encodeShortProperty(TOPIC_ALIAS, message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

            final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
            for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
                encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
            }
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            final ByteBuffer payload = message.getWrapped().getRawPayload();
            if (payload != null) {
                out.writeBytes(payload);
            }
        }

    }

}
