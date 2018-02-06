package org.mqttbee.mqtt5.exceptions;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Mqtt5MaximumPacketSizeExceededExceptionTest {

    @Test
    public void fillInStackTrace() {
        final Mqtt5MaximumPacketSizeExceededException exception =
                new Mqtt5MaximumPacketSizeExceededException(new Mqtt5Message() {
                    @Override
                    public void encode(
                            @NotNull Channel channel, @NotNull ByteBuf out) {

                    }

                    @Override
                    public int encodedLength(int maxPacketSize) {
                        return 0;
                    }
                }, 100);
        exception.fillInStackTrace();
        assertEquals(0, exception.getStackTrace().length);
    }
}