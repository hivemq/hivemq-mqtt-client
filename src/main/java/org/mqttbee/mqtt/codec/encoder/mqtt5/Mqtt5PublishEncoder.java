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
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.publish.MqttPublish.NO_MESSAGE_EXPIRY;
import static org.mqttbee.mqtt.message.publish.MqttPublishProperty.*;
import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishEncoder extends Mqtt5MessageWithUserPropertiesEncoder<MqttStatefulPublish> {

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBLISH.getCode() << 4;

    @Inject
    Mqtt5PublishEncoder() {}

    @Override
    int remainingLengthWithoutProperties(final @NotNull MqttStatefulPublish message) {
        final MqttPublish stateless = message.stateless();

        int remainingLength = 0;

        if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || message.isNewTopicAlias()) {
            remainingLength += stateless.getTopic().encodedLength();
        } else {
            remainingLength = MqttBinaryData.EMPTY_LENGTH;
        }

        if (stateless.getQos() != MqttQos.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = stateless.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    int propertyLength(final @NotNull MqttStatefulPublish message) {
        int propertyLength = 0;

        propertyLength += fixedPropertyLength(message.stateless());
        propertyLength += omissiblePropertyLength(message);

        propertyLength += shortPropertyEncodedLength(message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            propertyLength += variableByteIntegerPropertyEncodedLength(subscriptionIdentifiers.get(i));
        }

        return propertyLength;
    }

    final int fixedPropertyLength(final @NotNull MqttPublish publish) {
        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(publish.getRawMessageExpiryInterval(), NO_MESSAGE_EXPIRY);
        propertyLength += nullablePropertyEncodedLength(publish.getRawPayloadFormatIndicator());
        propertyLength += nullablePropertyEncodedLength(publish.getRawContentType());
        propertyLength += nullablePropertyEncodedLength(publish.getRawResponseTopic());
        propertyLength += nullablePropertyEncodedLength(publish.getRawCorrelationData());

        return propertyLength;
    }

    @Override
    @NotNull ByteBuf encode(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBufAllocator allocator,
            final int encodedLength, final int remainingLength, final int propertyLength, final int omittedProperties) {

        final ByteBuffer payload = message.stateless().getRawPayload();
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
    void encode(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int remainingLength,
            final int propertyLength, final int omittedProperties) {

        encodeFixedHeader(message, out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int remainingLength) {

        final MqttPublish stateless = message.stateless();

        int flags = 0;
        if (message.isDup()) {
            flags |= 0b1000;
        }
        flags |= stateless.getQos().getCode() << 1;
        if (stateless.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        final MqttPublish stateless = message.stateless();

        if ((message.getTopicAlias() == DEFAULT_NO_TOPIC_ALIAS) || message.isNewTopicAlias()) {
            stateless.getTopic().encode(out);
        } else {
            MqttBinaryData.encodeEmpty(out);
        }

        if (stateless.getQos() != MqttQos.AT_MOST_ONCE) {
            out.writeShort(message.getPacketIdentifier());
        }

        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);

        encodeFixedProperties(message.stateless(), out);
        encodeOmissibleProperties(message, out, omittedProperties);

        encodeShortProperty(TOPIC_ALIAS, message.getTopicAlias(), DEFAULT_NO_TOPIC_ALIAS, out);

        final ImmutableIntArray subscriptionIdentifiers = message.getSubscriptionIdentifiers();
        for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, subscriptionIdentifiers.get(i), out);
        }
    }

    final void encodeFixedProperties(
            final @NotNull MqttPublish publish, final @NotNull ByteBuf out) {

        encodeIntProperty(MESSAGE_EXPIRY_INTERVAL, publish.getRawMessageExpiryInterval(), NO_MESSAGE_EXPIRY, out);
        encodeNullableProperty(PAYLOAD_FORMAT_INDICATOR, publish.getRawPayloadFormatIndicator(), out);
        encodeNullableProperty(CONTENT_TYPE, publish.getRawContentType(), out);
        encodeNullableProperty(RESPONSE_TOPIC, publish.getRawResponseTopic(), out);
        encodeNullableProperty(CORRELATION_DATA, publish.getRawCorrelationData(), out);
    }

    private void encodePayload(final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out) {
        final ByteBuffer payload = message.stateless().getRawPayload();
        if ((payload != null) && !payload.isDirect()) {
            out.writeBytes(payload.duplicate());
        }
    }
}
