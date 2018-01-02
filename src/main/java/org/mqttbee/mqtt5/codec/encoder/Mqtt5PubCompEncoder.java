package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder implements Mqtt5MessageEncoder<Mqtt5PubCompInternal> {

    public void encode(
            @NotNull final Mqtt5PubCompInternal pubComp, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
