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
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.NewMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.encodeVariableByteIntegerProperty;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.variableByteIntegerPropertyEncodedLength;
import static org.mqttbee.mqtt.message.subscribe.MqttSubscribeProperty.SUBSCRIPTION_IDENTIFIER;
import static org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeEncoder extends Mqtt5WrappedMessageEncoder<MqttSubscribe, MqttSubscribeWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttSubscribe, MqttSubscribeWrapper, MqttMessageEncoderProvider<MqttSubscribeWrapper>>
            PROVIDER = NewMqttWrappedMessageEncoderProvider.create(Mqtt5SubscribeEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttSubscription> subscriptions = message.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            remainingLength += subscriptions.get(i).getTopicFilter().encodedLength() + 1;
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttSubscribeWrapper wrapper) {
        return Mqtt5SubscribeWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5SubscribeWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttSubscribeWrapper, MqttSubscribe, MqttMessageEncoderProvider<MqttSubscribeWrapper>, Mqtt5SubscribeEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttSubscribeWrapper, MqttSubscribe, Mqtt5SubscribeEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5SubscribeWrapperEncoder::new);

        private static final int FIXED_HEADER = (Mqtt5MessageType.SUBSCRIBE.getCode() << 4) | 0b0010;

        @Override
        int additionalPropertyLength(@NotNull final MqttSubscribeWrapper message) {
            return variableByteIntegerPropertyEncodedLength(message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        }

        @Override
        protected void encode(
                @NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out, final int remainingLength,
                final int propertyLength, final int omittedProperties) {

            encodeFixedHeader(out, remainingLength);
            encodeVariableHeader(message, out, propertyLength, omittedProperties);
            encodePayload(message, out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
            out.writeByte(FIXED_HEADER);
            MqttVariableByteInteger.encode(remainingLength, out);
        }

        private void encodeVariableHeader(
                @NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            out.writeShort(message.getPacketIdentifier());
            encodeProperties(message, out, propertyLength, omittedProperties);
        }

        private void encodeProperties(
                @NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            MqttVariableByteInteger.encode(propertyLength, out);
            encodeVariableByteIntegerProperty(SUBSCRIPTION_IDENTIFIER, message.getSubscriptionIdentifier(),
                    DEFAULT_NO_SUBSCRIPTION_IDENTIFIER, out);
            encodeOmissibleProperties(message, out, omittedProperties);
        }

        private void encodePayload(@NotNull final MqttSubscribeWrapper message, @NotNull final ByteBuf out) {
            final ImmutableList<MqttSubscription> subscriptions = message.getWrapped().getSubscriptions();
            for (int i = 0; i < subscriptions.size(); i++) {
                final MqttSubscription subscription = subscriptions.get(i);

                subscription.getTopicFilter().to(out);

                int subscriptionOptions = 0;
                subscriptionOptions |= subscription.getRetainHandling().getCode() << 4;
                if (subscription.isRetainAsPublished()) {
                    subscriptionOptions |= 0b0000_1000;
                }
                if (subscription.isNoLocal()) {
                    subscriptionOptions |= 0b0000_0100;
                }
                subscriptionOptions |= subscription.getQoS().getCode();

                out.writeByte(subscriptionOptions);
            }
        }

    }

}
