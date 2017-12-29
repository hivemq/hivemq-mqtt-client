package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5Disconnect;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectEncoder {

    public void encode(@NotNull final Mqtt5Disconnect disconnect, @NotNull final ByteBuf out) {

    }

}
