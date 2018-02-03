package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;

/**
 * MQTT 5 UNSUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Unsubscribe {

    /**
     * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic Filter.
     */
    @NotNull
    ImmutableList<? extends Mqtt5TopicFilter> getTopicFilters();

    /**
     * @return the optional user properties of this UNSUBSCRIBE packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

}
