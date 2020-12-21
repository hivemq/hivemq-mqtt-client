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

import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubscribeEncoder extends Mqtt5MessageWithUserPropertiesEncoder<MqttStatefulUnsubscribe> {

    private static final int FIXED_HEADER = (Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Inject
    Mqtt5UnsubscribeEncoder() {}

    @Override
    int remainingLengthWithoutProperties(final @NotNull MqttStatefulUnsubscribe message) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttTopicFilterImpl> topicFilters = message.stateless().getTopicFilters();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        return remainingLength;
    }

    @Override
    int propertyLength(final @NotNull MqttStatefulUnsubscribe message) {
        return omissiblePropertyLength(message);
    }

    @Override
    void encode(
            final @NotNull MqttStatefulUnsubscribe message,
            final @NotNull ByteBuf out,
            final int remainingLength,
            final int propertyLength,
            final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            final @NotNull MqttStatefulUnsubscribe message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

        out.writeShort(message.getPacketIdentifier());
        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            final @NotNull MqttStatefulUnsubscribe message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);
        encodeOmissibleProperties(message, out, omittedProperties);
    }

    private void encodePayload(final @NotNull MqttStatefulUnsubscribe message, final @NotNull ByteBuf out) {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = message.stateless().getTopicFilters();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < topicFilters.size(); i++) {
            topicFilters.get(i).encode(out);
        }
    }
}
