package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.auth.Mqtt5Auth;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder implements Mqtt5MessageEncoder<Mqtt5Auth> {

    public void encode(@NotNull final Mqtt5Auth auth, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
