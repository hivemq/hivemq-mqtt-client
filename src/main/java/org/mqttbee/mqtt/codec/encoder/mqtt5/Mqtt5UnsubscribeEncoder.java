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
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeEncoder extends Mqtt5WrappedMessageEncoder<MqttUnsubscribe, MqttUnsubscribeWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttUnsubscribe, MqttUnsubscribeWrapper, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>>
            PROVIDER = NewMqttWrappedMessageEncoderProvider.create(Mqtt5UnsubscribeEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final ImmutableList<MqttTopicFilterImpl> topicFilters = message.getTopicFilters();
        for (int i = 0; i < topicFilters.size(); i++) {
            remainingLength += topicFilters.get(i).encodedLength();
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        return message.getUserProperties().encodedLength();
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttUnsubscribeWrapper wrapper) {
        return Mqtt5UnsubscribeWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5UnsubscribeWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttUnsubscribeWrapper, MqttUnsubscribe, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>, Mqtt5UnsubscribeEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttUnsubscribeWrapper, MqttUnsubscribe, Mqtt5UnsubscribeEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5UnsubscribeWrapperEncoder::new);

        private static final int FIXED_HEADER = (Mqtt5MessageType.UNSUBSCRIBE.getCode() << 4) | 0b0010;

        @Override
        protected void encode(
                @NotNull final MqttUnsubscribeWrapper message, @NotNull final ByteBuf out, final int remainingLength,
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
                @NotNull final MqttUnsubscribeWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            out.writeShort(message.getPacketIdentifier());
            encodeProperties(message, out, propertyLength, omittedProperties);
        }

        private void encodeProperties(
                @NotNull final MqttUnsubscribeWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            MqttVariableByteInteger.encode(propertyLength, out);
            encodeOmissibleProperties(message, out, omittedProperties);
        }

        private void encodePayload(@NotNull final MqttUnsubscribeWrapper message, @NotNull final ByteBuf out) {
            final ImmutableList<MqttTopicFilterImpl> topicFilters = message.getWrapped().getTopicFilters();
            for (int i = 0; i < topicFilters.size(); i++) {
                topicFilters.get(i).to(out);
            }
        }

    }

}
