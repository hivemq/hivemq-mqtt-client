package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.publish.Mqtt5Publish;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PublishDecoder implements Mqtt5MessageDecoder {

    @Override
    @Nullable
    public Mqtt5Publish decode(final int flags, final int remainingLength, @NotNull final ByteBuf in) {
        final boolean dup = (flags & 0b1000) != 0;
        final int qos = (flags & 0b0110) >> 1;
        final boolean retain = (flags & 0b0001) != 0;

        return null;
    }

}
