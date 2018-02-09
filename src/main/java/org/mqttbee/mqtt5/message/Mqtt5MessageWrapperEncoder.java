package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5MessageEncoder.Mqtt5MessageWithPropertiesEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageWrapper.Mqtt5WrappedMessage;

import java.util.function.Function;

/**
 * Base class for MQTT message wrappers with user properties in its variable header.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5MessageWrapperEncoder<T extends Mqtt5MessageWrapper>
        extends Mqtt5MessageWithPropertiesEncoder<T> {

    public Mqtt5MessageWrapperEncoder(@NotNull final T wrapper) {
        super(wrapper);
    }

    @Override
    protected final int calculateEncodedRemainingLength() {
        return message.getWrapped().getEncoder().encodedRemainingLengthWithoutProperties() +
                additionalRemainingLength();
    }

    /**
     * Calculates the additional remaining length byte count of this MQTT message.
     *
     * @return the additional remaining length of the wrapper.
     */
    protected int additionalRemainingLength() {
        return 0;
    }

    @Override
    protected final int calculateEncodedPropertyLength() {
        return message.getWrapped().getEncoder().encodedPropertyLength() + additionalPropertyLength();
    }

    /**
     * Calculates the additional property length byte count of this MQTT message.
     *
     * @return the additional property length of the wrapper.
     */
    protected int additionalPropertyLength() {
        return 0;
    }

    @Override
    Mqtt5UserPropertiesImpl getUserProperties() {
        return message.getWrapped().getUserProperties();
    }

    /**
     * Base class for wrapped MQTT messages with user properties in its variable header.
     */
    public static abstract class Mqtt5WrappedMessageEncoder<T extends Mqtt5WrappedMessage<T, W>, W extends Mqtt5MessageWrapper<W, T>> {

        protected final T message;
        private int remainingLengthWithoutProperties = -1;
        private int propertyLength = -1;

        public Mqtt5WrappedMessageEncoder(@NotNull final T message) {
            this.message = message;
        }

        final int encodedRemainingLengthWithoutProperties() {
            if (remainingLengthWithoutProperties == -1) {
                remainingLengthWithoutProperties = calculateEncodedRemainingLengthWithoutProperties();
            }
            return remainingLengthWithoutProperties;
        }

        /**
         * Calculates the remaining length byte count without the properties of this MQTT message.
         *
         * @return the encoded remaining length without the properties of this MQTT message.
         */
        protected abstract int calculateEncodedRemainingLengthWithoutProperties();

        final int encodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Calculates the property length byte count of this MQTT message.
         *
         * @return the encoded property length of this MQTT message.
         */
        protected abstract int calculateEncodedPropertyLength();

        public abstract Function<W, ? extends Mqtt5MessageWrapperEncoder<W>> wrap();

    }

}
