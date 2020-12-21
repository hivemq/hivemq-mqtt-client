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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author David Katz
 */
abstract class AbstractMqtt5EncoderWithUserPropertiesTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE = (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    private final @NotNull MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
    private final @NotNull MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
    final int userPropertyBytes = 1 // identifier
            + 2 // key length
            + 4 // bytes to encode "user"
            + 2 // value length
            + 8; // bytes to encode "property"

    private final @NotNull MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);

    AbstractMqtt5EncoderWithUserPropertiesTest(
            final @NotNull MqttMessageEncoders messageEncoders, final boolean connected) {

        super(messageEncoders, connected);
    }

    @NotNull MqttUserPropertiesImpl getUserProperties(final int totalCount) {
        final ImmutableList.Builder<MqttUserPropertyImpl> builder = ImmutableList.builder();
        for (int i = 0; i < totalCount; i++) {
            builder.add(userProperty);
        }
        return MqttUserPropertiesImpl.of(builder.build());
    }

    abstract int getMaxPropertyLength();

    @NotNull MqttUtf8StringImpl getPaddedUtf8String(final int length) {
        final char[] reasonString = new char[length];
        Arrays.fill(reasonString, 'r');
        return MqttUtf8StringImpl.of(new String(reasonString));
    }

    class MaximumPacketBuilder {

        private final @NotNull ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        private final int maxUserPropertyCount;
        private final int remainingPropertyBytes;

        MaximumPacketBuilder() {
            // MQTT v5.0 Spec §3.4.1
            final int maxPropertyLength = getMaxPropertyLength();

            remainingPropertyBytes = maxPropertyLength % userPropertyBytes;

            maxUserPropertyCount = maxPropertyLength / userPropertyBytes;

            userPropertiesBuilder = ImmutableList.builder();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < maxUserPropertyCount; i++) {
                userPropertiesBuilder.add(userProperty);
            }
        }

        @NotNull MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return getTooManyUserProperties(0);
        }

        @NotNull MqttUserPropertiesImpl getTooManyUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        int getMaxUserPropertiesCount() {
            return maxUserPropertyCount;
        }

        int getRemainingPropertyBytes() {
            return remainingPropertyBytes;
        }

        @NotNull MqttUtf8StringImpl getPaddedUtf8StringTooLong() {
            return getPaddedUtf8String(getRemainingPropertyBytes() - 2 + 1);
        }
    }
}
