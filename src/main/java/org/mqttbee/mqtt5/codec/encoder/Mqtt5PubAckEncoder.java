package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckEncoder implements Mqtt5MessageEncoder<Mqtt5PubAckInternal> {

    public void encode(@NotNull final Mqtt5PubAckInternal pubAck, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
