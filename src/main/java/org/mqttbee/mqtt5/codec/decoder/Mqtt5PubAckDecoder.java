package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAck;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0;

    @Override
    @Nullable
    public Mqtt5PubAck decode(final int flags, final int remainingLength, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        return null;
    }

}
