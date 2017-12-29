package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubComp;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder {

    public void encode(@NotNull final Mqtt5PubComp pubComp, @NotNull final ByteBuf out) {

    }

}
