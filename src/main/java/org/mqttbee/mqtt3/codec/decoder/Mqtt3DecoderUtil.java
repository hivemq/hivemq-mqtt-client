package org.mqttbee.mqtt3.codec.decoder;

import io.netty.channel.Channel;

class Mqtt3DecoderUtil {


    public static void disconnectUngracefully(final Channel channel) {
        //TODO
        channel.close();
    }
}
