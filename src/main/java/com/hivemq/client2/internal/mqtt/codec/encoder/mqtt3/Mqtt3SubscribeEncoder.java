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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3SubscribeEncoder extends Mqtt3MessageEncoder<MqttStatefulSubscribe> {

    private static final int FIXED_HEADER = (Mqtt3MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Inject
    Mqtt3SubscribeEncoder() {}

    @Override
    int remainingLength(final @NotNull MqttStatefulSubscribe message) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscription> subscriptions = message.stateless().getSubscriptions();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1; // QoS
        }

        return remainingLength;
    }

    @Override
    void encode(final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out, final int remainingLength) {
        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

    private void encodePayload(final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out) {
        final ImmutableList<MqttSubscription> subscriptions = message.stateless().getSubscriptions();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);
            subscription.getTopicFilter().encode(out);
            out.writeByte(subscription.getMaxQos().getCode());
        }
    }
}
