package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAck;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckEncoder {

    public void encode(@NotNull final Mqtt5PubAck pubAck, @NotNull final ByteBuf out) {

    }

}
