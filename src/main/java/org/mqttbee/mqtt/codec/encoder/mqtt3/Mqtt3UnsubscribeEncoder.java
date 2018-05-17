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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3UnsubscribeEncoder extends Mqtt3MessageEncoder<MqttStatefulUnsubscribe> {

    private static final int FIXED_HEADER = (Mqtt3MessageType.UNSUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Inject
    Mqtt3UnsubscribeEncoder() {
    }

    @Override
    int remainingLength(@NotNull final MqttStatefulUnsubscribe message) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttTopicFilterImpl> subscriptions = message.getStatelessMessage().getTopicFilters();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).encodedLength();
        }

        return remainingLength;
    }

    @Override
    public void encode(
            @NotNull final MqttStatefulUnsubscribe message, @NotNull final ByteBuf out, final int remainingLength) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(@NotNull final MqttStatefulUnsubscribe message, @NotNull final ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

    private void encodePayload(@NotNull final MqttStatefulUnsubscribe message, @NotNull final ByteBuf out) {
        final ImmutableList<MqttTopicFilterImpl> subscriptions = message.getStatelessMessage().getTopicFilters();
        for (int i = 0; i < subscriptions.size(); i++) {
            subscriptions.get(i).to(out);
        }
    }

}
