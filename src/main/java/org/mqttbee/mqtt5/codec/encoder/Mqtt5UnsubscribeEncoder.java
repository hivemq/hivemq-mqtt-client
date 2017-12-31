package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubscribeEncoder implements Mqtt5MessageEncoder<Mqtt5Unsubscribe> {

    public void encode(
            @NotNull final Mqtt5Unsubscribe unsubscribe, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
