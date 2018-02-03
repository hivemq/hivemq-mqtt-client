package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.MustNotBeImplementedException;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 * @see Mqtt5UserProperty
 */
public class Mqtt5UserPropertyImpl implements Mqtt5UserProperty {

    /**
     * Checks if the given User Property is instance of this implementation.
     *
     * @param userProperty the User Property.
     * @return the casted User Property.
     * @throws MustNotBeImplementedException if the User Property is not instance of this implementation.
     */
    @NotNull
    public static Mqtt5UserPropertyImpl checkNotImplemented(@NotNull final Mqtt5UserProperty userProperty) {
        if (userProperty instanceof Mqtt5UserPropertyImpl) {
            return (Mqtt5UserPropertyImpl) userProperty;
        }
        throw new MustNotBeImplementedException(Mqtt5UserProperty.class);
    }

    /**
     * Creates an User Property of the given name and value.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the created User Property.
     */
    public static Mqtt5UserPropertyImpl of(@NotNull final Mqtt5UTF8String name, @NotNull final Mqtt5UTF8String value) {
        return new Mqtt5UserPropertyImpl(
                Mqtt5UTF8StringImpl.checkNotImplemented(name), Mqtt5UTF8StringImpl.checkNotImplemented(value));
    }

    /**
     * Validates and decodes a User Property from the given byte buffer at the current reader index.
     *
     * @param in the byte buffer to decode from.
     * @return the decoded User Property or null if the name and/or value are not valid UTF-8 encoded Strings.
     */
    @Nullable
    public static Mqtt5UserPropertyImpl decode(@NotNull final ByteBuf in) {
        final Mqtt5UTF8StringImpl name = Mqtt5UTF8StringImpl.from(in);
        if (name == null) {
            return null;
        }
        final Mqtt5UTF8StringImpl value = Mqtt5UTF8StringImpl.from(in);
        if (value == null) {
            return null;
        }
        return new Mqtt5UserPropertyImpl(name, value);
    }

    private final Mqtt5UTF8StringImpl name;
    private final Mqtt5UTF8StringImpl value;

    public Mqtt5UserPropertyImpl(@NotNull final Mqtt5UTF8StringImpl name, @NotNull final Mqtt5UTF8StringImpl value) {
        this.name = name;
        this.value = value;
    }

    @NotNull
    @Override
    public Mqtt5UTF8StringImpl getName() {
        return name;
    }

    @NotNull
    @Override
    public Mqtt5UTF8StringImpl getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt5UserPropertyImpl)) {
            return false;
        }
        final Mqtt5UserPropertyImpl that = (Mqtt5UserPropertyImpl) o;
        return name.equals(that.name) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

}
