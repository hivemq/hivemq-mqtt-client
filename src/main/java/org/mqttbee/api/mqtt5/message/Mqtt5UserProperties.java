package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

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
        return Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;
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

        return Mqtt5UserPropertiesImpl.of(userProperties);
    }

    /**
     * @return the User Properties as an immutable list.
     */
    @NotNull
    ImmutableList<? extends Mqtt5UserProperty> asList();

}
