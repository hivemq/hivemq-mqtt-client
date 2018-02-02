package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * MQTT User Property according to the MQTT 5 specification.
 * <p>
 * A User Property consists of a name and value UTF-8 encoded String Pair.
 *
 * @author Silvio Giebl
 */
public class Mqtt5UserProperty {

    /**
     * Validates and decodes a User Property from the given byte buffer at the current reader index.
     *
     * @param in the byte buffer to decode from.
     * @return the decoded User Property or null if the name and/or value are not valid UTF-8 encoded Strings.
     */
    @Nullable
    public static Mqtt5UserProperty decode(@NotNull final ByteBuf in) {
        final Mqtt5UTF8StringImpl name = Mqtt5UTF8StringImpl.from(in);
        if (name == null) {
            return null;
        }
        final Mqtt5UTF8StringImpl value = Mqtt5UTF8StringImpl.from(in);
        if (value == null) {
            return null;
        }
        return new Mqtt5UserProperty(name, value);
    }

    private final Mqtt5UTF8StringImpl name;
    private final Mqtt5UTF8StringImpl value;

    public Mqtt5UserProperty(@NotNull final Mqtt5UTF8StringImpl name, @NotNull final Mqtt5UTF8StringImpl value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name of this User Property.
     */
    @NotNull
    public Mqtt5UTF8StringImpl getName() {
        return name;
    }

    /**
     * @return the value of this User Property.
     */
    @NotNull
    public Mqtt5UTF8StringImpl getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt5UserProperty)) {
            return false;
        }
        final Mqtt5UserProperty that = (Mqtt5UserProperty) o;
        return name.equals(that.name) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

}
