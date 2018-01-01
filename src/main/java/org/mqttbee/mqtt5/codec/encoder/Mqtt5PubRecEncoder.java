package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRecEncoder implements Mqtt5MessageEncoder<Mqtt5PubRecImpl> {

    public void encode(@NotNull final Mqtt5PubRecImpl pubRec, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
