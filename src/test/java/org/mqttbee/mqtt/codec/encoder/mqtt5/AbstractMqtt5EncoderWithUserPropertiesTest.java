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

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * @author David Katz
 */
abstract class AbstractMqtt5EncoderWithUserPropertiesTest extends AbstractMqtt5EncoderTest {
    static final private int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE = (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
    private final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
    private final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
    final int userPropertyBytes = 1 // identifier
            + 2 // key length
            + 4 // bytes to encode "user"
            + 2 // value length
            + 8; // bytes to encode "property"

    final private MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);

    AbstractMqtt5EncoderWithUserPropertiesTest(
            @NotNull final MqttMessageEncoders messageEncoders, final boolean connected) {

        super(messageEncoders, connected);
    }

    MqttUserPropertiesImpl getUserProperties(final int totalCount) {
        final ImmutableList.Builder<MqttUserPropertyImpl> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < totalCount; i++) {
            builder.add(userProperty);
        }
        return MqttUserPropertiesImpl.of(builder.build());
    }

    abstract int getMaxPropertyLength();

    MqttUTF8StringImpl getPaddedUtf8String(final int length) {
        final char[] reasonString = new char[length];
        Arrays.fill(reasonString, 'r');
        return MqttUTF8StringImpl.from(new String(reasonString));
    }

    class MaximumPacketBuilder {
        int maxUserPropertyCount;

        int remainingPropertyBytes;


        MaximumPacketBuilder build() {
            // MQTT v5.0 Spec §3.4.1
            final int maxPropertyLength = getMaxPropertyLength();


            remainingPropertyBytes = maxPropertyLength % userPropertyBytes;

            maxUserPropertyCount = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < maxUserPropertyCount; i++) {
                userPropertiesBuilder.add(userProperty);
            }

            return this;
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return getTooManyUserProperties(0);
        }

        MqttUserPropertiesImpl getTooManyUserProperties(final int withExtraUserProperties) {
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

        MqttUTF8StringImpl getPaddedUtf8StringTooLong() {
            return getPaddedUtf8String(getRemainingPropertyBytes() - 2 + 1);
        }
    }
}
