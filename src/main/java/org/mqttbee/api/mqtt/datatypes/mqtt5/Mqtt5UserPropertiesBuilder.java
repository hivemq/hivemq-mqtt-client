package org.mqttbee.api.mqtt.datatypes.mqtt5;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertiesBuilder {

    private ImmutableList.Builder<MqttUserPropertyImpl> listBuilder;

    Mqtt5UserPropertiesBuilder() {
    }

    Mqtt5UserPropertiesBuilder(@NotNull final Mqtt5UserProperties userProperties) {
        final MqttUserPropertiesImpl userPropertiesImpl =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
        listBuilder = ImmutableList.builder();
        listBuilder.addAll(userPropertiesImpl.asList());
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder add(@NotNull final Mqtt5UserProperty userProperty) {
        if (listBuilder == null) {
            listBuilder = ImmutableList.builder();
        }
        listBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class));
        return this;
    }

    @NotNull
    public Mqtt5UserProperties build() {
        return MqttUserPropertiesImpl.build(listBuilder);
    }

}
