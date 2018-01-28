package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserProperties {

    /**
     * Empty list of User Properties.
     */
    public static final Mqtt5UserProperties DEFAULT_NO_USER_PROPERTIES = of(ImmutableList.of());

    /**
     * Creates User Properties from the given immutable list.
     *
     * @param userProperties the immutable list of User Properties.
     * @return the created User Properties.
     */
    public static Mqtt5UserProperties of(@NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        return new Mqtt5UserProperties(userProperties);
    }

    /**
     * Builds User Properties from the given builder.
     *
     * @param userPropertiesBuilder the builder for the User Properties.
     * @return the built User Properties or {@link #DEFAULT_NO_USER_PROPERTIES} if the builder is null.
     */
    public static Mqtt5UserProperties build(
            @Nullable final ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder) {
        return (userPropertiesBuilder == null) ? DEFAULT_NO_USER_PROPERTIES : of(userPropertiesBuilder.build());
    }

    private final ImmutableList<Mqtt5UserProperty> userProperties;
    private int encodedLength = -1;

    private Mqtt5UserProperties(@NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.userProperties = userProperties;
    }

    /**
     * @return the User Properties as an immutable list.
     */
    @NotNull
    public ImmutableList<Mqtt5UserProperty> asList() {
        return userProperties;
    }

    /**
     * Encodes this User Properties to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if name and value can not be encoded due to byte count restrictions. This check is
     * performed with the method {@link #encodedLength()} which is generally called before this method.
     *
     * @param out the byte buffer to encode to.
     */
    public void encode(@NotNull final ByteBuf out) {
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
     * Calculates the byte count of this User Properties according to the MQTT 5 specification.
     *
     * @return the encoded length of this User Properties.
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
                final Mqtt5UserProperty userProperty = userProperties.get(i);
                encodedLength += 1 +
                        userProperty.getName().encodedLength() +
                        userProperty.getValue().encodedLength();
            }
        }
        return encodedLength;
    }

}
