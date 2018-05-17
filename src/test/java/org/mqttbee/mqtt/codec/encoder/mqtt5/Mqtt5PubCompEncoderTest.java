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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode.SUCCESS;
import static org.mqttbee.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;

/**
 * @author David Katz
 */
class Mqtt5PubCompEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    Mqtt5PubCompEncoderTest() {
        super(code -> new Mqtt5PubCompEncoder(), true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        final MqttUTF8StringImpl reasonString = null;
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubComp pubComp = new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, reasonString, userProperties);

        encode(expected, pubComp);
    }

    @Test
    void encode_reasonCodeOmittedWhenSuccessWithoutProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final MqttPubComp pubComp = new MqttPubComp(5, SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, pubComp);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubCompReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_reasonCodes(final Mqtt5PubCompReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                6, 5,
                //   reason code placeholder
                (byte) 0xFF
        };

        expected[4] = (byte) reasonCode.getCode();
        final MqttPubComp pubComp =
                new MqttPubComp(0x0605, reasonCode, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        encode(expected, pubComp);
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                13,
                // variable header
                //   packet identifier
                0, 9,
                //   reason code
                (byte) 0x92,
                //   properties
                9,
                // reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'
        };

        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubComp pubComp = new MqttPubComp(9, PACKET_IDENTIFIER_NOT_FOUND, reasonString, userProperties);

        encode(expected, pubComp);
    }

    @Test
    void encode_userProperty() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                21,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                17,
                // user Property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubComp pubComp = new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, null, userProperties);

        encode(expected, pubComp);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        createServerConnectionData(3);

        final MqttPubComp pubComp = new MqttPubComp(1, SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        final Throwable exception =
                assertThrows(MqttMaximumPacketSizeExceededException.class, () -> channel.writeOutbound(pubComp));
        assertTrue(exception.getMessage().contains("packet size exceeded for AUTH"));
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92
        };
        createServerConnectionData(expected.length + 2);

        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubComp pubComp = new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, null, userProperties);

        encode(expected, pubComp);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitReasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                21,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                17,
                // user Property
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };
        createServerConnectionData(expected.length + 2);

        final MqttUserPropertiesImpl userProperties = getUserProperties(1);
        final MqttPubComp pubComp =
                new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, MqttUTF8StringImpl.from("reason"), userProperties);
        encode(expected, pubComp);
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92
        };

        final MqttUserPropertiesImpl userProperties =
                getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes) + 1);


        final MqttPubComp pubComp = new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, null, userProperties);
        encode(expected, pubComp);
    }

    @Test
    void encode_propertyLengthExceeded_omitReasonString() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttUserPropertiesImpl userProperties =
                getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / userPropertyBytes));
        final int reasonStringTooLong = (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE % userPropertyBytes) + 1;
        final MqttUTF8StringImpl reasonString = getPaddedUtf8String(reasonStringTooLong);

        final ByteBuf expected = Unpooled.buffer(
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes(),
                MAXIMUM_PACKET_SIZE_LIMIT - maxPacket.getRemainingPropertyBytes());

        // fixed header
        // type, reserved
        expected.writeByte(0b0111_0000);
        // remaining length (2 + 1 + 4 + (userPropertyBytes * maxPossibleUserPropertiesCount) = 268435447
        expected.writeByte(0xf7);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // packet identifier
        expected.writeByte(0);
        expected.writeByte(5);
        // reason code
        expected.writeByte(0x92);
        // properties length
        expected.writeByte(0xf0);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // user properties
        userProperties.encode(expected);

        final MqttPubComp pubComp = new MqttPubComp(5, PACKET_IDENTIFIER_NOT_FOUND, reasonString, userProperties);
        encode(expected.array(), pubComp);
        expected.release();
    }

    private void encode(final byte[] expected, final MqttPubComp pubComp) {
        encode(pubComp, expected);
    }

    int getMaxPropertyLength() {
        return MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 4  // property length
                - 2  // packet identifier
                - 1; // reason code
    }
}
