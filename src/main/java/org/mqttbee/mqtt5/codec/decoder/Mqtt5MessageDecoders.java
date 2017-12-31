package org.mqttbee.mqtt5.codec.decoder;

import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageDecoders {

    @Nullable
    Mqtt5MessageDecoder get(final int code);

}
