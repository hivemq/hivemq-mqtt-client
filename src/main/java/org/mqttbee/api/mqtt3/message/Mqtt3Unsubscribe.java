package org.mqttbee.api.mqtt3.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;

public interface Mqtt3Unsubscribe {

    /**
     * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic Filter.
     */
    @NotNull
    ImmutableList<Mqtt5TopicFilter> getTopicFilters();


}
