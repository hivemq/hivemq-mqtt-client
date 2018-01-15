package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

/**
 * MQTT 5 UNSUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Unsubscribe {

    /**
     * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic Filter.
     */
    @NotNull
    ImmutableList<Mqtt5TopicFilter> getTopicFilters();

    /**
     * @return the optional user properties of this UNSUBSCRIBE packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
