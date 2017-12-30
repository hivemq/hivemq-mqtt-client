package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserProperty {

    public static final List<Mqtt5UserProperty> NO_USER_PROPERTIES = Collections.unmodifiableList(new ArrayList<>(0));

    public static void encode(@NotNull final List<Mqtt5UserProperty> userProperties, @NotNull final ByteBuf byteBuf) {
        if (userProperties != Mqtt5UserProperty.NO_USER_PROPERTIES) {
            for (final Mqtt5UserProperty userProperty : userProperties) {
                byteBuf.writeByte(Mqtt5Property.USER_PROPERTY);
                userProperty.getName().to(byteBuf);
                userProperty.getValue().to(byteBuf);
            }
        }
    }

    public static int encodedLength(@NotNull final List<Mqtt5UserProperty> userProperties) {
        int encodedLength = 0;
        if (userProperties != Mqtt5UserProperty.NO_USER_PROPERTIES) {
            for (final Mqtt5UserProperty userProperty : userProperties) {
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
