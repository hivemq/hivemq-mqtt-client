package org.mqttbee.api.mqtt3.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

public interface Mqtt3SubAck {

    /**
     * @return the reason codes of this SUBACK packet, each belonging to a subscription in the corresponding SUBSCRIBE
     * packet in the same order.
     */
    @NotNull
    ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes();


}
