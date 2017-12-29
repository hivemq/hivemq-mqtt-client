package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5Subscribe;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5SubscribeEncoder {

    public void encode(@NotNull final Mqtt5Subscribe subscribe, @NotNull final ByteBuf out) {

    }

}
