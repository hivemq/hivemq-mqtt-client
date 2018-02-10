package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage.Mqtt5MessageWrapper;

import java.util.function.Function;

/**
 * Base class for encoders of wrapped MQTT messages with User Properties.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5WrappedMessageEncoder<T extends Mqtt5WrappedMessage<T, W>, W extends Mqtt5MessageWrapper<W, T>> {

    protected final T message;
    private int remainingLengthWithoutProperties = -1;
    private int propertyLength = -1;

    Mqtt5WrappedMessageEncoder(@NotNull final T message) {
        this.message = message;
    }

    /**
     * Returns the remaining length byte count without the properties of the wrapped MQTT message. Calculation is only
     * performed if necessary.
     *
     * @return the remaining length without the properties of the wrapped MQTT message.
     */
    final int encodedRemainingLengthWithoutProperties() {
        if (remainingLengthWithoutProperties == -1) {
            remainingLengthWithoutProperties = calculateEncodedRemainingLengthWithoutProperties();
        }
        return remainingLengthWithoutProperties;
    }

    /**
     * Calculates the remaining length byte count without the properties of the wrapped MQTT message.
     *
     * @return the remaining length without the properties of the wrapped MQTT message.
     */
    abstract int calculateEncodedRemainingLengthWithoutProperties();

    /**
     * Returns the property length byte count of the wrapped MQTT message. Calculation is only performed if necessary.
     *
     * @return the property length of the wrapped MQTT message.
     */
    final int encodedPropertyLength() {
        if (propertyLength == -1) {
            propertyLength = calculateEncodedPropertyLength();
        }
        return propertyLength;
    }

    /**
     * Calculates the property length byte count of the wrapped MQTT message.
     *
     * @return the property length of the wrapped MQTT message.
     */
    abstract int calculateEncodedPropertyLength();

    /**
     * Encodes the properties of the wrapped MQTT message which must not be omitted by the wrapper.
     */
    void encodeFixedProperties(@NotNull final ByteBuf out) {
        // default no op
    }

    /**
     * @return a new encoder for a wrapper around the MQTT message.
     */
    public abstract Function<W, ? extends Mqtt5MessageWrapperEncoder<W>> wrap();


    /**
     * Base class for encoders of wrappers around MQTT messages with User Properties.
     */
    public abstract static class Mqtt5MessageWrapperEncoder<T extends Mqtt5MessageWrapper>
            extends Mqtt5MessageWithPropertiesEncoder<T> {

        Mqtt5MessageWrapperEncoder(@NotNull final T wrapper) {
            super(wrapper);
        }

        @Override
        final int calculateRemainingLength() {
            return message.getWrapped().getEncoder().encodedRemainingLengthWithoutProperties() +
                    additionalRemainingLength();
        }

        /**
         * Calculates the additional remaining length byte count of the wrapper around the MQTT message.
         *
         * @return the additional remaining length of the wrapper.
         */
        int additionalRemainingLength() {
            return 0;
        }

        @Override
        final int calculatePropertyLength() {
            return message.getWrapped().getEncoder().encodedPropertyLength() + additionalPropertyLength();
        }

        /**
         * Calculates the additional property length byte count of the wrapper around the MQTT message.
         *
         * @return the additional property length of the wrapper.
         */
        int additionalPropertyLength() {
            return 0;
        }

        @Override
        Mqtt5UserPropertiesImpl getUserProperties() {
            return message.getWrapped().getUserProperties();
        }

    }

}
