/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt.message.publish.MqttPublishProperty.*;
import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishEncoder extends Mqtt5MessageWithUserPropertiesEncoder<MqttPublishWrapper> {

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

    @Inject
    Mqtt5PublishEncoder() {
    }

    @Override
    int remainingLengthWithoutProperties(@NotNull final MqttPublishWrapper message) {
        final MqttPublish wrapped = message.getWrapped();
        int remainingLength = 0;

        if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || message.isNewTopicAlias()) {
            remainingLength += wrapped.getTopic().encodedLength();
        } else {
            remainingLength = MqttBinaryData.EMPTY_LENGTH;
        }

        if (wrapped.getQos() != MqttQoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = wrapped.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    int propertyLength(@NotNull final MqttPublishWrapper message) {
        final MqttPublish wrapped = message.getWrapped();
        int propertyLength = 0;

        propertyLength += fixedPropertyLength(wrapped);

        propertyLength += wrapped.getUserProperties().encodedLength();

        propertyLength += shortPropertyEncodedLength(message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            propertyLength += variableByteIntegerPropertyEncodedLength(subscriptionIdentifiers.get(i));
        }

        return propertyLength;
    }

    final int fixedPropertyLength(@NotNull final MqttPublish wrapped) {
        int propertyLength = 0;

        propertyLength +=
                intPropertyEncodedLength(wrapped.getRawMessageExpiryInterval(), MESSAGE_EXPIRY_INTERVAL_INFINITY);
        propertyLength += nullablePropertyEncodedLength(wrapped.getRawPayloadFormatIndicator());
        propertyLength += nullablePropertyEncodedLength(wrapped.getRawContentType());
        propertyLength += nullablePropertyEncodedLength(wrapped.getRawResponseTopic());
        propertyLength += nullablePropertyEncodedLength(wrapped.getRawCorrelationData());

        return propertyLength;
    }

    @NotNull
    @Override
    protected ByteBuf encode(
            @NotNull final MqttPublishWrapper message, @NotNull final ByteBufAllocator allocator,
            final int encodedLength, final int remainingLength, final int propertyLength, final int omittedProperties) {

        final ByteBuffer payload = message.getWrapped().getRawPayload();
        if ((payload != null) && payload.isDirect()) {
            final int encodedLengthWithoutPayload = encodedLength - payload.remaining();
            final ByteBuf out = allocator.ioBuffer(encodedLengthWithoutPayload, encodedLengthWithoutPayload);
            encode(message, out, remainingLength, propertyLength, omittedProperties);
            return Unpooled.unmodifiableBuffer(out, Unpooled.wrappedBuffer(payload));
        }
        final ByteBuf out = allocator.ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength, propertyLength, omittedProperties);
        return out;
    }

    @Override
    protected void encode(
            @NotNull final MqttPublishWrapper message, @NotNull final ByteBuf out, final int remainingLength,
            final int propertyLength, final int omittedProperties) {

        encodeFixedHeader(message, out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(
            @NotNull final MqttPublishWrapper message, @NotNull final ByteBuf out, final int remainingLength) {

        final MqttPublish wrapped = message.getWrapped();

        int flags = 0;
        if (message.isDup()) {
            flags |= 0b1000;
        }
        flags |= wrapped.getQos().getCode() << 1;
        if (wrapped.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final MqttPublishWrapper message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        final MqttPublish wrapped = message.getWrapped();

        if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || message.isNewTopicAlias()) {
            wrapped.getTopic().to(out);
        } else {
            MqttBinaryData.encodeEmpty(out);
        }

        if (wrapped.getQos() != MqttQoS.AT_MOST_ONCE) {
            out.writeShort(message.getPacketIdentifier());
        }

        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            @NotNull final MqttPublishWrapper message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);

        encodeFixedProperties(message.getWrapped(), out);
        encodeOmissibleProperties(message, out, omittedProperties);

        encodeShortProperty(TOPIC_ALIAS, message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
        }
    }

    final void encodeFixedProperties(
            @NotNull final MqttPublish wrapped, @NotNull final ByteBuf out) {

        encodeIntProperty(MESSAGE_EXPIRY_INTERVAL, wrapped.getRawMessageExpiryInterval(),
                MESSAGE_EXPIRY_INTERVAL_INFINITY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, wrapped.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, wrapped.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, wrapped.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, wrapped.getRawCorrelationData(), out);
    }

    private void encodePayload(@NotNull final MqttPublishWrapper message, @NotNull final ByteBuf out) {
        final ByteBuffer payload = message.getWrapped().getRawPayload();
        if ((payload != null) && !payload.isDirect()) {
            out.writeBytes(payload.duplicate());
        }
    }

}
