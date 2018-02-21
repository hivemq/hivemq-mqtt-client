package org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;

public interface Mqtt3SubAck {

    /**
     * @return the reason codes of this SUBACK packet, each belonging to a subscription in the corresponding SUBSCRIBE
     * packet in the same order.
     */
    @NotNull
    ImmutableList<Mqtt3SubAckReturnCode> getReasonCodes();

}
