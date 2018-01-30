package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;

/**
 * MQTT User Property according to the MQTT 5 specification.
 * <p>
 * A User Property consists of a name and value UTF-8 encoded String Pair.
 *
 * @author Silvio Giebl
 */
public class Mqtt5UserProperty {

    /**
     * Empty list of User Properties.
     */
    public static final ImmutableList<Mqtt5UserProperty> DEFAULT_NO_USER_PROPERTIES = ImmutableList.of();

    /**
     * Validates and decodes a User Property from the given byte buffer at the current reader index.
     *
     * @param in the byte buffer to decode from.
     * @return the decoded User Property or null if the name and/or value are not valid UTF-8 encoded Strings.
     */
    @Nullable
    public static Mqtt5UserProperty decode(@NotNull final ByteBuf in) {
        final Mqtt5UTF8String name = Mqtt5UTF8String.from(in);
        if (name == null) {
            return null;
        }
        final Mqtt5UTF8String value = Mqtt5UTF8String.from(in);
        if (value == null) {
            return null;
        }
        return new Mqtt5UserProperty(name, value);
    }

    /**
     * Encodes the given list of User Properties to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if name and value can not be encoded due to byte count restrictions. This check is
     * performed with the method {@link #encodedLength(ImmutableList)} which is generally called before this method.
     *
     * @param userProperties the list of User Properties to encode.
     * @param out            the byte buffer to encode to.
     */
    public static void encode(
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties, @NotNull final ByteBuf out) {
        if (!userProperties.isEmpty()) {
            for (int i = 0; i < userProperties.size(); i++) {
                final Mqtt5UserProperty userProperty = userProperties.get(i);
                out.writeByte(Mqtt5Property.USER_PROPERTY);
                userProperty.getName().to(out);
                userProperty.getValue().to(out);
            }
        }
    }

    /**
     * Calculates the byte count of the given list of User Properties according to the MQTT 5 specification.
     *
     * @param userProperties the list of User Properties to calculate the byte count for.
     * @return the encoded length of the given list of User Properties.
     * @throws Mqtt5BinaryDataExceededException if name and/or value can not be encoded due to byte count restrictions.
     */
    public static int encodedLength(@NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        int encodedLength = 0;
        if (!userProperties.isEmpty()) {
            for (int i = 0; i < userProperties.size(); i++) {
                final Mqtt5UserProperty userProperty = userProperties.get(i);
                encodedLength += 1 + userProperty.getName().encodedLength() + userProperty.getValue().encodedLength();
            }
        }
        return encodedLength;
    }

    /**
     * Builds a list of User Properties from the given builder.
     *
     * @param userPropertiesBuilder the builder for the list of User Properties.
     * @return the built list of User Properties or {@link #DEFAULT_NO_USER_PROPERTIES} if the builder is null.
     */
    @NotNull
    public static ImmutableList<Mqtt5UserProperty> build(
            @Nullable final ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder) {
        return (userPropertiesBuilder == null) ? DEFAULT_NO_USER_PROPERTIES : userPropertiesBuilder.build();
    }

    private final Mqtt5UTF8String name;
    private final Mqtt5UTF8String value;

    public Mqtt5UserProperty(@NotNull final Mqtt5UTF8String name, @NotNull final Mqtt5UTF8String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name of this User Property.
     */
    @NotNull
    public Mqtt5UTF8String getName() {
        return name;
    }

    /**
     * @return the value of this User Property.
     */
    @NotNull
    public Mqtt5UTF8String getValue() {
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
