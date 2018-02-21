package org.mqttbee.api.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.mqtt5.Mqtt5UserProperties;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;

/**
 * MQTT 5 UNSUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Unsubscribe extends Mqtt5Message {

    @NotNull
    static Mqtt5UnsubscribeBuilder build() {
        return new Mqtt5UnsubscribeBuilder();
    }

    /**
     * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic Filter.
     */
    @NotNull
    ImmutableList<? extends MqttTopicFilter> getTopicFilters();

    /**
     * @return the optional user properties of this UNSUBSCRIBE packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
