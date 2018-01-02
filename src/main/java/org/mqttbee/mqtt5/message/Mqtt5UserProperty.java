package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserProperty {

    public static final ImmutableList<Mqtt5UserProperty> DEFAULT_NO_USER_PROPERTIES = ImmutableList.of();

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

    public static int encodedLength(@NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        int encodedLength = 0;
        if (!userProperties.isEmpty()) {
            for (int i = 0; i < userProperties.size(); i++) {
                final Mqtt5UserProperty userProperty = userProperties.get(i);
                encodedLength += 1 +
                        userProperty.getName().toBinary().length +
                        userProperty.getValue().toBinary().length;
            }
        }
        return encodedLength;
    }

    private final Mqtt5UTF8String name;
    private final Mqtt5UTF8String value;

    public Mqtt5UserProperty(@NotNull final Mqtt5UTF8String name, @NotNull final Mqtt5UTF8String value) {
        this.name = name;
        this.value = value;
    }

    @NotNull
    public Mqtt5UTF8String getName() {
        return name;
    }

    @NotNull
    public Mqtt5UTF8String getValue() {
        return value;
    }

}
