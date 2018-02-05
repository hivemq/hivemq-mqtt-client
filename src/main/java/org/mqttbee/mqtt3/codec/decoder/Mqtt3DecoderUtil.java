package org.mqttbee.mqtt3.codec.decoder;

import io.netty.channel.Channel;

public class Mqtt3DecoderUtil {


    public static void disconnectUngracefully(Channel channel) {
        //TODO
        channel.close();
    }
}
