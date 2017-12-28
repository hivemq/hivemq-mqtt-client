package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingResp;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PingRespDecoder implements Mqtt5MessageDecoder {

    @Override
    public Mqtt5PingResp decode(final int flags, final int remainingLength, final ByteBuf in) {
        return null;
    }

}
