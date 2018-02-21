package org.mqttbee.api.mqtt.datatypes.mqtt5;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * Collection of {@link Mqtt5UserProperty User Properties}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5UserProperties {

    /**
     * @return the empty collection of User Properties.
     */
    @NotNull
    static Mqtt5UserProperties of() {
        return MqttUserPropertiesImpl.NO_USER_PROPERTIES;
    }

    /**
     * Creates a collection of User Properties of the given User Properties.
     *
     * @param userProperties the User Properties.
     * @return the created collection of User Properties.
     */
    @NotNull
    static Mqtt5UserProperties of(@NotNull final Mqtt5UserProperty... userProperties) {
        Preconditions.checkNotNull(userProperties);

        final ImmutableList.Builder<MqttUserPropertyImpl> builder =
                ImmutableList.builderWithExpectedSize(userProperties.length);
        for (final Mqtt5UserProperty userProperty : userProperties) {
            builder.add(MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class));
        }
        return MqttUserPropertiesImpl.of(builder.build());
    }

    @NotNull
    static Mqtt5UserPropertiesBuilder builder() {
        return new Mqtt5UserPropertiesBuilder();
    }

    @NotNull
    static Mqtt5UserPropertiesBuilder extend(@NotNull final Mqtt5UserProperties userProperties) {
        return new Mqtt5UserPropertiesBuilder(userProperties);
    }

    /**
     * @return the User Properties as an immutable list.
     */
    @NotNull
    ImmutableList<? extends Mqtt5UserProperty> asList();

}
