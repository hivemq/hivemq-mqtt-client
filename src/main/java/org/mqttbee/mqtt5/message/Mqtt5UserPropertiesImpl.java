package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.MustNotBeImplementedException;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;

/**
 * @author Silvio Giebl
 * @see Mqtt5UserProperties
 */
public class Mqtt5UserPropertiesImpl implements Mqtt5UserProperties {

    /**
     * Empty collection of User Properties.
     */
    public static final Mqtt5UserPropertiesImpl NO_USER_PROPERTIES = new Mqtt5UserPropertiesImpl(ImmutableList.of());

    /**
     * Checks if the given collection of User Properties is instance of this implementation.
     *
     * @param userProperties the collection of User Property.
     * @return the casted collection of User Properties.
     * @throws MustNotBeImplementedException if the collection of User Property is not instance of this implementation.
     */
    @NotNull
    public static Mqtt5UserPropertiesImpl checkNotImplemented(@NotNull final Mqtt5UserProperties userProperties) {
        if (userProperties instanceof Mqtt5UserPropertiesImpl) {
            return (Mqtt5UserPropertiesImpl) userProperties;
        }
        throw new MustNotBeImplementedException(Mqtt5UserProperties.class);
    }

    /**
     * Creates a collection of User Properties from the given User Properties.
     *
     * @param userProperties the User Properties.
     * @return the created collection of User Properties.
     */
    @NotNull
    public static Mqtt5UserPropertiesImpl of(@NotNull final Mqtt5UserProperty... userProperties) {
        final ImmutableList.Builder<Mqtt5UserPropertyImpl> builder =
                ImmutableList.builderWithExpectedSize(userProperties.length);
        for (final Mqtt5UserProperty userProperty : userProperties) {
            builder.add(Mqtt5UserPropertyImpl.checkNotImplemented(userProperty));
        }
        return of(builder.build());
    }

    /**
     * Creates a collection of User Properties from the given immutable list of User Properties.
     *
     * @param userProperties the immutable list of User Properties.
     * @return the created collection of User Properties or {@link #NO_USER_PROPERTIES} if the list is empty.
     */
    @NotNull
    public static Mqtt5UserPropertiesImpl of(@NotNull final ImmutableList<Mqtt5UserPropertyImpl> userProperties) {
        return userProperties.isEmpty() ? NO_USER_PROPERTIES : new Mqtt5UserPropertiesImpl(userProperties);
    }

    /**
     * Builds a collection of User Properties from the given builder.
     *
     * @param userPropertiesBuilder the builder for the User Properties.
     * @return the built collection of User Properties or {@link #NO_USER_PROPERTIES} if the builder is null.
     */
    @NotNull
    public static Mqtt5UserPropertiesImpl build(
            @Nullable final ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder) {
        return (userPropertiesBuilder == null) ? NO_USER_PROPERTIES : of(userPropertiesBuilder.build());
    }

    private final ImmutableList<Mqtt5UserPropertyImpl> userProperties;
    private int encodedLength = -1;

    private Mqtt5UserPropertiesImpl(@NotNull final ImmutableList<Mqtt5UserPropertyImpl> userProperties) {
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserPropertyImpl> asList() {
        return userProperties;
    }

    /**
     * Encodes this collection of User Properties to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if name and value can not be encoded due to byte count restrictions. This check is
     * performed with the method {@link #encodedLength()} which is generally called before this method.
     *
     * @param out the byte buffer to encode to.
     */
    public void encode(@NotNull final ByteBuf out) {
        if (!userProperties.isEmpty()) {
            for (int i = 0; i < userProperties.size(); i++) {
                final Mqtt5UserPropertyImpl userProperty = userProperties.get(i);
                out.writeByte(Mqtt5Property.USER_PROPERTY);
                userProperty.getName().to(out);
                userProperty.getValue().to(out);
            }
        }
    }

    /**
     * Calculates the byte count of this collection of User Properties according to the MQTT 5 specification.
     *
     * @return the encoded length of this collection of User Properties.
     * @throws Mqtt5BinaryDataExceededException if name and/or value can not be encoded due to byte count restrictions.
     */
    public int encodedLength() {
        if (encodedLength == -1) {
            encodedLength = calculateEncodedLength();
        }
        return encodedLength;
    }

    private int calculateEncodedLength() {
        int encodedLength = 0;
        if (!userProperties.isEmpty()) {
            for (int i = 0; i < userProperties.size(); i++) {
                final Mqtt5UserPropertyImpl userProperty = userProperties.get(i);
                encodedLength += 1 + userProperty.getName().encodedLength() + userProperty.getValue().encodedLength();
            }
        }
        return encodedLength;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt5UserPropertiesImpl)) {
            return false;
        }
        final Mqtt5UserPropertiesImpl that = (Mqtt5UserPropertiesImpl) o;
        return userProperties.equals(that.userProperties);
    }

    @Override
    public int hashCode() {
        return userProperties.hashCode();
    }

}
