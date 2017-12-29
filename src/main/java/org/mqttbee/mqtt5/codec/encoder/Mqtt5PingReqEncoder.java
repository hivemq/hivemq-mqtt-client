package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReq;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PingReqEncoder {

    public void encode(@NotNull final Mqtt5PingReq pingReq, @NotNull final ByteBuf out) {

    }

}
