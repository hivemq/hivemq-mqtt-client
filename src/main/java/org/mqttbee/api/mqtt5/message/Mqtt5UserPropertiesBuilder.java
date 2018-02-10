package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertiesBuilder {

    private ImmutableList.Builder<Mqtt5UserPropertyImpl> listBuilder;

    Mqtt5UserPropertiesBuilder() {
    }

    Mqtt5UserPropertiesBuilder(@NotNull final Mqtt5UserProperties userProperties) {
        final Mqtt5UserPropertiesImpl userPropertiesImpl =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        listBuilder = ImmutableList.builder();
        listBuilder.addAll(userPropertiesImpl.asList());
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder add(@NotNull final Mqtt5UserProperty userProperty) {
        if (listBuilder == null) {
            listBuilder = ImmutableList.builder();
        }
        listBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(userProperty, Mqtt5UserPropertyImpl.class));
        return this;
    }

    @NotNull
    public Mqtt5UserProperties build() {
        return Mqtt5UserPropertiesImpl.build(listBuilder);
    }

}
