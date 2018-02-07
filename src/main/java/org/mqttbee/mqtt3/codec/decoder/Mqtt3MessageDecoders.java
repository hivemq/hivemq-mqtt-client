package org.mqttbee.mqtt3.codec.decoder;

import org.mqttbee.annotations.Nullable;

/**
 * Collection of decoders for MQTT messages which can be queried by the MQTT message type code.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3MessageDecoders {

    /**
     * Returns the corresponding decoder to the given MQTT message type code.
     *
     * @param code the MQTT message type code.
     * @return the corresponding decoder to the MQTT message type or null if there is no decoder for the MQTT message
     * type code.
     */
    @Nullable
    Mqtt3MessageDecoder get(final int code);

}
