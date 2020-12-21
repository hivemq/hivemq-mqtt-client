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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client2.internal.util.collections.ImmutableIntList;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5PublishDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PublishDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.PUBLISH.getCode()] = new Mqtt5PublishDecoder();
        }});
    }

    @Test
    void decode_allProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0011,
                //   remaining length
                72,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                52,
                //     payload format indicator
                0x01, 0,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     topic alias
                0x23, 0, 3,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                //     subscription identifier
                0x0B, 123,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',
                // payload
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final MqttStatefulPublish publishInternal = decodeInternal(encoded);

        assertNotNull(publishInternal);

        assertEquals(false, publishInternal.isDup());
        assertEquals(12, publishInternal.getPacketIdentifier());
        assertEquals(3, publishInternal.getTopicAlias());

        final ImmutableIntList subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.size());
        assertEquals(123, subscriptionIdentifiers.get(0));

        final MqttPublish publish = publishInternal.stateless();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(MqttQos.AT_LEAST_ONCE, publish.getQos());
        assertEquals(true, publish.isRetain());
        assertTrue(publish.getMessageExpiryInterval().isPresent());
        assertEquals(10, publish.getMessageExpiryInterval().getAsLong());
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UNSPECIFIED, publish.getPayloadFormatIndicator().get());
        assertTrue(publish.getContentType().isPresent());
        assertEquals("text", publish.getContentType().get().toString());
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("response", publish.getResponseTopic().get().toString());
        assertTrue(publish.getCorrelationData().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{5, 4, 3, 2, 1}), publish.getCorrelationData().get());

        final ImmutableList<MqttUserPropertyImpl> userProperties = publish.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertTrue(publish.getPayload().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), publish.getPayload().get());
    }

    @Test
    void decode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                20,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 0,
                // payload
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final MqttStatefulPublish publishInternal = decodeInternal(encoded);

        assertEquals(false, publishInternal.isDup());

        final ImmutableIntList subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(0, subscriptionIdentifiers.size());

        final MqttPublish publish = publishInternal.stateless();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(MqttQos.AT_MOST_ONCE, publish.getQos());
        assertEquals(true, publish.isRetain());
        assertFalse(publish.getMessageExpiryInterval().isPresent());
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UNSPECIFIED, publish.getPayloadFormatIndicator().get());

        assertTrue(publish.getPayload().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), publish.getPayload().get());
    }

    @Test
    void decode_minimal() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                4,
                // variable header
                //   topic name
                0, 1, 't',
                // properties
                0
        };
        final MqttPublish publish = decode(encoded);
        assertEquals("t", publish.getTopic().toString());
    }

    @Test
    void decode_tooShort() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_fixedHeaderQos() {
        final byte[] encodedQos0 = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final MqttPublish decodeQos0 = decode(encodedQos0);
        assertEquals(MqttQos.AT_MOST_ONCE, decodeQos0.getQos());

        final byte[] encodedQos1 = {
                // fixed header
                //   type, flags
                0b0011_0011,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final MqttPublish decodeQos1 = decode(encodedQos1);
        assertEquals(MqttQos.AT_LEAST_ONCE, decodeQos1.getQos());

        final byte[] encodedQos2 = {
                // fixed header
                //   type, flags
                0b0011_1101,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final MqttPublish decodeQos2 = decode(encodedQos2);
        assertEquals(MqttQos.EXACTLY_ONCE, decodeQos2.getQos());
    }

    @Test
    void decode_fixedHeaderQosInvalid_returnsNull() {
        final byte[] encodedQosInvalid = {
                // fixed header
                //   type, flags
                0b0011_0111,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encodedQosInvalid, MALFORMED_PACKET);
    }

    @Test
    void decode_dupTrueForQos0_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_1000,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_topicNameInvalidStringLength_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                3,
                // variable header
                //   topic name
                0, 5, 't'
        };
        decodeNok(encoded, TOPIC_NAME_INVALID);
    }

    @Test
    void decode_messageExpiryInterval() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                16,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                7,
                //     payload format indicator
                0x01, 1,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                // payload
                0x00
        };
        final MqttPublish publish = decode(encoded);
        assertTrue(publish.getMessageExpiryInterval().isPresent());
        assertEquals(10, publish.getMessageExpiryInterval().getAsLong());
    }

    @Test
    void decode_messageExpiryIntervalDuplicate_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                12,
                //     payload format indicator
                0x01, 1,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                // payload
                0x00
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_payloadFormatIndicatorUtf8() {
        validatePayloadFormat();
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                17,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 1,
                // payload
                (byte) 0xE4, (byte) 0xBD, (byte) 0xA0, 0x20, (byte) 0xE5, (byte) 0xA5, (byte) 0xBD
        };

        final MqttPublish publish = decode(encoded);
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UTF_8, publish.getPayloadFormatIndicator().get());
        assertTrue(publish.getPayload().isPresent());
        assertEquals("你 好", new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void decode_invalidPayloadFormatIndicator_returnsNull() {
        validatePayloadFormat();
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 3
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_payloadFormatIndicatorMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                4,
                //     payload format indicator
                0x01, 0,
                //     payload format indicator
                0x01, 0,
                // payload
                0x00
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_PayloadUtf8NotWellFormed_returnsNull() {
        validatePayloadFormat();
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 1,
                // payload
                (byte) 0xFF
        };
        decodeNok(encoded, PAYLOAD_FORMAT_INVALID);
    }

    @Test
    void decode_contentType() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                15,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                7,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',

        };
        final MqttPublish publish = decode(encoded);
        assertTrue(publish.getContentType().isPresent());
        assertEquals("text", publish.getContentType().get().toString());
    }

    @Test
    void decode_invalidContentType_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                3,
                //     content type
                0x03, 0, 4
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_userProperty() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                22,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                14,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'

        };
        final MqttPublish publish = decode(encoded);
        final ImmutableList<MqttUserPropertyImpl> userProperties = publish.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
    }

    @Test
    void decode_invalidUserProperty_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                3,
                //     user properties
                0x26, 0, 4

        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_subscriptionIdentifier() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                15,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier
                0x0B, 123,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        final ImmutableIntList subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.size());
        assertEquals(123, subscriptionIdentifiers.get(0));
    }

    @Test
    void decode_subscriptionIdentifierZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                15,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier
                0x0B, 0,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_subscriptionIdentifierNegative_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier negative (invalid)
                0x0B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_subscriptionIdentifierMax() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier max = 268435455
                0x0B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        final ImmutableIntList subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.size());
        assertEquals(268435455, subscriptionIdentifiers.get(0));
    }

    @Test
    void decode_subscriptionIdentifierMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier 128
                0x0B, (byte) 0x80, (byte) 0x01,
                //     subscription identifier 128
                0x0B, 16,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        final ImmutableIntList subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(2, subscriptionIdentifiers.size());
        assertEquals(128, subscriptionIdentifiers.get(0));
        assertEquals(16, subscriptionIdentifiers.get(1));
    }

    @Test
    void decode_packetIdentifierWithQos0isNotSet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        assertEquals(-1, publishInternal.getPacketIdentifier());
    }

    @Test
    void decode_packetIdentifierWithQos1() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        assertEquals(12, publishInternal.getPacketIdentifier());
    }

    @Test
    void decode_packetIdentifierWithQos2() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0100,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encoded);
        assertEquals(12, publishInternal.getPacketIdentifier());
    }

    @Test
    void decode_packetIdentifierMissingWithQos2() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0100,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_correlationData() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                29,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                21,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     payload format indicator
                0x01, 0
        };
        final MqttPublish publish = decode(encoded);
        assertTrue(publish.getCorrelationData().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{5, 4, 3, 2, 1}), publish.getCorrelationData().get());
    }

    @Test
    void decode_correlationDataMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                37,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                29,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     correlation data
                0x09, 0, 1, 2, 3, 4, 5, 6,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_responseTopic() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final MqttPublish publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("response", publish.getResponseTopic().get().toString());
    }

    @Test
    void decode_responseTopicMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                32,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                24,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e', //
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_responseTopicWithWildcards_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 't', 'o', 'p', 'i', 'c', '/', 'a',
                //     payload format indicator
                0x01, 0
        };
        MqttPublish publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/a", publish.getResponseTopic().get().toString());

        encoded[20] = '#';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[20] = 'b';
        publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/b", publish.getResponseTopic().get().toString());

        encoded[20] = '+';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[20] = 'c';
        publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/c", publish.getResponseTopic().get().toString());
    }

    @Test
    void decode_topicAlias() {
        final byte[] encodedWithTopicName = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encodedWithTopicName);
        assertEquals(3, publishInternal.getTopicAlias());

        final byte[] encodedWithTopicAliasOnly = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name is empty
                0, 0,
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternalTopicAliasOnly = decodeInternal(encodedWithTopicAliasOnly);
        assertEquals(3, publishInternalTopicAliasOnly.getTopicAlias());
    }

    @Test
    void decode_topicAliasDuplicate_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                16,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                8,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0,
                //     topic alias duplicate (error!)
                0x23, 0, 3
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_noTopicAliasFound_returnsNull() {
        final byte[] encodedWithTopicName = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final MqttStatefulPublish publishInternal = decodeInternal(encodedWithTopicName);
        assertEquals(3, publishInternal.getTopicAlias());

        final byte[] encodedWithWrongTopicAlias = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name is empty
                0, 0,
                //   properties
                5,
                //     topic alias
                0x23, 0, 1,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encodedWithWrongTopicAlias, TOPIC_ALIAS_INVALID);
    }

    @Test
    void decode_topicAliasZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 0,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, TOPIC_ALIAS_INVALID);
    }

    @Test
    void decode_topicAliasTooLarge_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias too large
                0x23, 0, 4,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, TOPIC_ALIAS_INVALID);
    }

    @Test
    void decode_topicWithWildcard_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 7, 't', 'o', 'p', 'i', 'c', '/', 'a',
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        MqttPublish decode = decode(encoded);
        assertEquals("topic/a", decode.getTopic().toString());

        encoded[10] = '#';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[10] = 'b';
        decode = decode(encoded);
        assertEquals("topic/b", decode.getTopic().toString());

        encoded[10] = '+';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[10] = 'c';
        decode = decode(encoded);
        assertEquals("topic/c", decode.getTopic().toString());
    }

    @Test
    void decode_negativePropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     invalid negative identifier
                (byte) 0xFF, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidPropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     invalid identifier
                (byte) 0x05, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthNegative_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties length negative
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthTooLong_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                3,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthTooShort_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                1,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_remainingLengthMissing_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_topicNullNoAlias_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                5,
                // variable header
                //   topic name null, no alias
                0, 0,
                //   properties length
                2,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    void decode_invalidMessageType_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                5,
                // variable header
                //   topic name null, no alias
                0, 0,
                //   properties length
                2,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @NotNull
    private MqttPublish decode(final @NotNull byte[] encoded) {
        return decodeInternal(encoded).stateless();
    }

    @NotNull
    private MqttStatefulPublish decodeInternal(final @NotNull byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final MqttStatefulPublish publishInternal = channel.readInbound();
        assertNotNull(publishInternal);

        return publishInternal;
    }

    private void decodeNok(final @NotNull byte[] encoded, final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final MqttStatefulPublish publishInternal = channel.readInbound();
        assertNull(publishInternal);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        createChannel();
    }
}
