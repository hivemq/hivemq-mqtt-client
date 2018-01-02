package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder implements Mqtt5MessageEncoder<Mqtt5PubRelInternal> {

    public void encode(@NotNull final Mqtt5PubRelInternal pubRel, @NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

}
