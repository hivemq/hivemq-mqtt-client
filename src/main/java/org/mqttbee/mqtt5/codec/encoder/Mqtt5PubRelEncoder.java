package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder implements Mqtt5MessageEncoder<Mqtt5PubRelImpl> {

    public void encode(@NotNull final Mqtt5PubRelImpl pubRel, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
