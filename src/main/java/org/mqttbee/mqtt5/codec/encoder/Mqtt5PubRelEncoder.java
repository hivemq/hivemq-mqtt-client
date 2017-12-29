package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRel;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder {

    public void encode(@NotNull final Mqtt5PubRel pubRel, @NotNull final ByteBuf out) {

    }

}
