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

package org.mqttbee.internal.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5MessageType;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.encodeVariableByteIntegerProperty;
import static org.mqttbee.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.variableByteIntegerPropertyEncodedLength;
import static org.mqttbee.internal.mqtt.message.subscribe.MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
import static org.mqttbee.internal.mqtt.message.subscribe.MqttSubscribeProperty.SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubscribeEncoder extends Mqtt5MessageWithUserPropertiesEncoder<MqttStatefulSubscribe> {

    private static final int FIXED_HEADER = (Mqtt5MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Inject
    Mqtt5SubscribeEncoder() {}

    @Override
    int remainingLengthWithoutProperties(final @NotNull MqttStatefulSubscribe message) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscription> subscriptions = message.stateless().getSubscriptions();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    @Override
    int propertyLength(final @NotNull MqttStatefulSubscribe message) {
        int propertyLength = 0;

        propertyLength += omissiblePropertyLength(message);
        propertyLength += variableByteIntegerPropertyEncodedLength(message.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);

        return propertyLength;
    }

    @Override
    void encode(
            final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out, final int remainingLength,
            final int propertyLength, final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        out.writeShort(message.getPacketIdentifier());
        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);
        encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, message.getSubscriptionIdentifier(),
                DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
        encodeOmissibleProperties(message, out, omittedProperties);
    }

    private void encodePayload(final @NotNull MqttStatefulSubscribe message, final @NotNull ByteBuf out) {
        final ImmutableList<MqttSubscription> subscriptions = message.stateless().getSubscriptions();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            final MqttSubscription subscription = subscriptions.get(i);

            subscription.getTopicFilter().encode(out);

            int subscriptionOptions = 0;
            subscriptionOptions |= subscription.getRetainHandling().getCode() << 4;
            if (subscription.isRetainAsPublished()) {
                subscriptionOptions |= 0b0000_1000;
            }
            if (subscription.isNoLocal()) {
                subscriptionOptions |= 0b0000_0100;
            }
            subscriptionOptions |= subscription.getQos().getCode();

            out.writeByte(subscriptionOptions);
        }
    }
}
