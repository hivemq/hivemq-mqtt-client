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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Base class for encoders of wrapped MQTT messages with User Properties.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5WrappedMessageEncoder<M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>>
        implements MqttWrappedMessageEncoder<M, W> {

    M message;
    private int remainingLengthWithoutProperties = -1;
    private int propertyLength = -1;

    @NotNull
    @Override
    public MqttWrappedMessageEncoder<M, W> apply(@NotNull final M message) {
        if (this.message != message) {
            remainingLengthWithoutProperties = propertyLength = -1;
        }
        this.message = message;
        return this;
    }

    /**
     * Returns the remaining length byte count without the properties of the wrapped MQTT message. Calculation is only
     * performed if necessary.
     *
     * @return the remaining length without the properties of the wrapped MQTT message.
     */
    final int remainingLengthWithoutProperties() {
        if (remainingLengthWithoutProperties == -1) {
            remainingLengthWithoutProperties = calculateRemainingLengthWithoutProperties();
        }
        return remainingLengthWithoutProperties;
    }

    /**
     * Calculates the remaining length byte count without the properties of the wrapped MQTT message.
     *
     * @return the remaining length without the properties of the wrapped MQTT message.
     */
    abstract int calculateRemainingLengthWithoutProperties();

    /**
     * Returns the property length byte count of the wrapped MQTT message. Calculation is only performed if necessary.
     *
     * @return the property length of the wrapped MQTT message.
     */
    final int propertyLength() {
        if (propertyLength == -1) {
            propertyLength = calculatePropertyLength();
        }
        return propertyLength;
    }

    /**
     * Calculates the property length byte count of the wrapped MQTT message.
     *
     * @return the property length of the wrapped MQTT message.
     */
    abstract int calculatePropertyLength();

    /**
     * Encodes the properties of the wrapped MQTT message which must not be omitted by the wrapper.
     */
    void encodeFixedProperties(@NotNull final ByteBuf out) {
        // default no op
    }


    /**
     * Base class for encoders of wrappers around MQTT messages with User Properties.
     */
    abstract static class Mqtt5MessageWrapperEncoder< //
            W extends MqttMessageWrapper<W, M, P>, M extends MqttWrappedMessage<M, W, P>, //
            P extends MqttMessageEncoderProvider<W>, E extends Mqtt5WrappedMessageEncoder<M, W>>
            extends Mqtt5MessageWithUserPropertiesEncoder<W> implements MqttMessageWrapperEncoderApplier<W, M, E> {

        E wrappedEncoder;

        @NotNull
        @Override
        public MqttMessageEncoder apply(@NotNull final W message, @NotNull final E wrappedEncoder) {
            this.wrappedEncoder = wrappedEncoder;
            return apply(message);
        }

        @Override
        final int calculateRemainingLength(@NotNull final W message) {
            return wrappedEncoder.remainingLengthWithoutProperties() + additionalRemainingLength(message);
        }

        /**
         * Calculates the additional remaining length byte count of the wrapper around the MQTT message.
         *
         * @return the additional remaining length of the wrapper.
         */
        int additionalRemainingLength(@NotNull final W message) {
            return 0;
        }

        @Override
        final int calculatePropertyLength(@NotNull final W message) {
            return wrappedEncoder.propertyLength() + additionalPropertyLength(message);
        }

        /**
         * Calculates the additional property length byte count of the wrapper around the MQTT message.
         *
         * @return the additional property length of the wrapper.
         */
        int additionalPropertyLength(@NotNull final W message) {
            return 0;
        }

        @Override
        final MqttUserPropertiesImpl getUserProperties(@NotNull final W message) {
            return message.getWrapped().getUserProperties();
        }

    }

}
