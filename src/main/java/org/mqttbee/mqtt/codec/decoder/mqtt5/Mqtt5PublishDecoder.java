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

package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.netty.ChannelAttributes;
import org.mqttbee.util.ByteBufferUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.*;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.publish.MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY;
import static org.mqttbee.mqtt.message.publish.MqttPublishProperty.*;
import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;
import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishDecoder implements MqttMessageDecoder {

    private static final int MIN_REMAINING_LENGTH = 3; // topic name (min 2) + property length (min 1)

    @Inject
    Mqtt5PublishDecoder() {
    }

    @Override
    @Nullable
    public MqttStatefulPublish decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException {

        final Channel channel = clientConnectionData.getChannel();

        final boolean dup = (flags & 0b1000) != 0;
        final MqttQoS qos = decodePublishQoS(flags, dup);
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
            topic = MqttTopicImpl.from(topicBinary);
            if (topic == null) {
                throw malformedTopic();
            }
        }

        final int packetIdentifier = decodePublishPacketIdentifier(qos, in);

        final int propertyLength = decodePropertyLength(in);

        long messageExpiryInterval = MESSAGE_EXPIRY_INTERVAL_INFINITY;
        Mqtt5PayloadFormatIndicator payloadFormatIndicator = null;
        MqttUTF8StringImpl contentType = null;
        MqttTopicImpl responseTopic = null;
        ByteBuffer correlationData = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;
        int topicAlias = DEFAULT_NO_TOPIC_ALIAS;
        TopicAliasUsage topicAliasUsage = TopicAliasUsage.HAS_NOT;
        ImmutableIntArray.Builder subscriptionIdentifiersBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case MESSAGE_EXPIRY_INTERVAL:
                    messageExpiryInterval = unsignedIntOnlyOnce(messageExpiryInterval, MESSAGE_EXPIRY_INTERVAL_INFINITY,
                            "message expiry interval", in);
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
                    responseTopic = MqttTopicImpl.from(in);
                    if (responseTopic == null) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed response topic");
                    }
                    break;

                case CORRELATION_DATA:
                    correlationData = decodeBinaryDataOnlyOnce(correlationData, "correlation data", in,
                            ChannelAttributes.useDirectBufferForCorrelationData(channel));
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
                    topicAliasUsage = TopicAliasUsage.HAS;
                    break;

                case SUBSCRIPTION_IDENTIFIER:
                    if (subscriptionIdentifiersBuilder == null) {
                        subscriptionIdentifiersBuilder = ImmutableIntArray.builder();
                    }
                    final int subscriptionIdentifier = MqttVariableByteInteger.decode(in);
                    if (subscriptionIdentifier < 0) {
                        throw new MqttDecoderException("malformed subscription identifier");
                    }
                    if (subscriptionIdentifier == 0) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
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

        boolean isNewTopicAlias = false;
        if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
            final MqttTopicImpl[] topicAliasMapping = clientConnectionData.getTopicAliasMapping();
            if ((topicAliasMapping == null) || (topicAlias > topicAliasMapping.length)) {
                throw new MqttDecoderException(
                        Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID,
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
                isNewTopicAlias = true;
            }
        } else if (topic == null) {
            throw new MqttDecoderException(
                    Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID,
                    "topic alias must be present if topic name is zero length");
        }

        final int payloadLength = in.readableBytes();
        ByteBuffer payload = null;
        if (payloadLength > 0) {
            payload = ByteBufferUtil.allocate(payloadLength, ChannelAttributes.useDirectBufferForPayload(channel));
            in.readBytes(payload);
            payload.position(0);

            if (payloadFormatIndicator == Mqtt5PayloadFormatIndicator.UTF_8) {
                if (ChannelAttributes.validatePayloadFormat(channel)) {
                    if (!Utf8.isWellFormed(ByteBufferUtil.getBytes(payload))) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PAYLOAD_FORMAT_INVALID,
                                "payload is not valid UTF-8");
                    }
                }
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        final MqttPublish publish =
                new MqttPublish(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator, contentType,
                        responseTopic, correlationData, topicAliasUsage, userProperties);

        final ImmutableIntArray subscriptionIdentifiers =
                (subscriptionIdentifiersBuilder == null) ? DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS :
                        subscriptionIdentifiersBuilder.build();

        return publish.createStateful(packetIdentifier, dup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
    }

}
