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
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3SubscribeEncoder extends Mqtt3MessageEncoder<MqttSubscribeWrapper> {

    private static final int FIXED_HEADER = (Mqtt3MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Inject
    Mqtt3SubscribeEncoder() {
    }

    @Override
    int calculateRemainingLength(@NotNull final MqttSubscribeWrapper message) {
        final MqttSubscribe wrapped = message.getWrapped();

        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscription> subscriptions = wrapped.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);
            remainingLength += subscription.getTopicFilter().encodedLength() + 1; // QoS
        }

        return remainingLength;
    }

    @Override
    public void encode(
            @NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out, final int remainingLength) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(@NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

    private void encodePayload(@NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out) {
        final MqttSubscribe wrapped = message.getWrapped();

        final ImmutableList<MqttSubscription> subscriptions = wrapped.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);
            subscription.getTopicFilter().to(out);
            out.writeByte(subscription.getQoS().getCode());
        }
    }

}
