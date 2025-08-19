/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5;

import com.hivemq.mqtt.client2.datatypes.MqttQos;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderContext;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderException;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoder;
import com.hivemq.mqtt.client2.internal.collections.ImmutableIntList;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.datatypes.*;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublish;
import com.hivemq.mqtt.client2.internal.message.publish.MqttStatefulPublish;
import com.hivemq.mqtt.client2.internal.util.ByteBufferUtil;
import com.hivemq.mqtt.client2.internal.util.Utf8Util;
import com.hivemq.mqtt.client2.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.*;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttPublish.NO_MESSAGE_EXPIRY;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttPublishProperty.*;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt5PublishDecoder INSTANCE = new Mqtt5PublishDecoder();

    private static final int MIN_REMAINING_LENGTH = 3; // topic name (min 2) + property length (min 1)

    private Mqtt5PublishDecoder() {}

    @Override
    public @NotNull MqttStatefulPublish decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

        final boolean dup = (flags & 0b1000) != 0;
        final MqttQos qos = decodePublishQos(flags, dup);
        final boolean retain = (flags & 0b0001) != 0;

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final byte[] topicBinary = MqttBinaryData.decode(in);
        if (topicBinary == null) {
            throw malformedTopic();
        }
        MqttTopicImpl topic = null;
        if (topicBinary.length != 0) {
            topic = MqttTopicImpl.of(topicBinary);
            if (topic == null) {
                throw malformedTopic();
            }
        }

        final int packetIdentifier = decodePublishPacketIdentifier(qos, in);

        final int propertyLength = decodePropertyLength(in);

        long messageExpiryInterval = NO_MESSAGE_EXPIRY;
        Mqtt5PayloadFormatIndicator payloadFormatIndicator = null;
        MqttUtf8StringImpl contentType = null;
        MqttTopicImpl responseTopic = null;
        ByteBuffer correlationData = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;
        int topicAlias = DEFAULT_NO_TOPIC_ALIAS;
        ImmutableIntList.Builder subscriptionIdentifiersBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case MESSAGE_EXPIRY_INTERVAL:
                    messageExpiryInterval =
                            unsignedIntOnlyOnce(messageExpiryInterval, NO_MESSAGE_EXPIRY, "message expiry interval",
                                    in);
                    break;

                case PAYLOAD_FORMAT_INDICATOR:
                    final short payloadFormatIndicatorByte =
                            unsignedByteOnlyOnce(payloadFormatIndicator != null, "payload format indicator", in);
                    payloadFormatIndicator = Mqtt5PayloadFormatIndicator.fromCode(payloadFormatIndicatorByte);
                    if (payloadFormatIndicator == null) {
                        throw new MqttDecoderException("wrong payload format indicator: " + payloadFormatIndicatorByte);
                    }
                    break;

                case CONTENT_TYPE:
                    contentType = decodeUTF8StringOnlyOnce(contentType, "content type", in);
                    break;

                case RESPONSE_TOPIC:
                    if (responseTopic != null) {
                        throw moreThanOnce("response topic");
                    }
                    responseTopic = MqttTopicImpl.decode(in);
                    if (responseTopic == null) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed response topic");
                    }
                    break;

                case CORRELATION_DATA:
                    correlationData = decodeBinaryDataOnlyOnce(correlationData, "correlation data", in,
                            context.useDirectBufferCorrelationData());
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, in);
                    break;

                case TOPIC_ALIAS:
                    topicAlias = unsignedShortOnlyOnce(topicAlias, DEFAULT_NO_TOPIC_ALIAS, "topic alias", in);
                    if (topicAlias == 0) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID, "topic alias must not be 0");
                    }
                    break;

                case SUBSCRIPTION_IDENTIFIER:
                    if (subscriptionIdentifiersBuilder == null) {
                        subscriptionIdentifiersBuilder = ImmutableIntList.builder();
                    }
                    final int subscriptionIdentifier = MqttVariableByteInteger.decode(in);
                    if (subscriptionIdentifier < 0) {
                        throw new MqttDecoderException("malformed subscription identifier");
                    }
                    if (subscriptionIdentifier == 0) {
                        throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                                "subscription identifier must not be 0");
                    }
                    subscriptionIdentifiersBuilder.add(subscriptionIdentifier);
                    break;

                default:
                    throw wrongProperty(propertyIdentifier);
            }
        }

        if (readPropertyLength != propertyLength) {
            throw malformedPropertyLength();
        }

        if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
            final MqttTopicImpl[] topicAliasMapping = context.getTopicAliasMapping();
            if ((topicAliasMapping == null) || (topicAlias > topicAliasMapping.length)) {
                throw new MqttDecoderException(Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID,
                        "topic alias must not exceed topic alias maximum");
            }
            if (topic == null) {
                topic = topicAliasMapping[topicAlias - 1];
                if (topic == null) {
                    throw new MqttDecoderException(
                            Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID, "topic alias has no mapping");
                }
            } else {
                topicAliasMapping[topicAlias - 1] = topic;
                topicAlias |= TOPIC_ALIAS_FLAG_NEW;
            }
        } else if (topic == null) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "topic alias must be present if topic name is zero length");
        }

        final int payloadLength = in.readableBytes();
        ByteBuffer payload = ByteBufferUtil.EMPTY_BYTE_BUFFER;
        if (payloadLength > 0) {
            payload = ByteBufferUtil.allocate(payloadLength, context.useDirectBufferPayload());
            in.readBytes(payload);
            payload.position(0);

            if ((payloadFormatIndicator == Mqtt5PayloadFormatIndicator.UTF_8) && context.validatePayloadFormat() &&
                    (Utf8Util.isWellFormed(ByteBufferUtil.getBytes(payload)) != 0)) {
                throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PAYLOAD_FORMAT_INVALID,
                        "payload is not valid UTF-8");
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        final MqttPublish publish =
                new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator, contentType,
                        responseTopic, correlationData, userProperties, null);

        final ImmutableIntList subscriptionIdentifiers =
                (subscriptionIdentifiersBuilder == null) ? DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS :
                        subscriptionIdentifiersBuilder.build();

        return publish.createStateful(packetIdentifier, dup, topicAlias, subscriptionIdentifiers);
    }
}
