package org.mqttbee.mqtt5;

import io.netty.util.AttributeKey;

/**
 * @author Silvio Giebl
 */
public class ChannelAttributes {

    public static final AttributeKey<Integer> MAXIMUM_INCOMING_PACKET_SIZE_KEY = AttributeKey.valueOf("packet.in.size.max");
    public static final AttributeKey<Integer> MAXIMUM_OUTGOING_PACKET_SIZE_KEY = AttributeKey.valueOf("packet.out.size.max");

    public static final AttributeKey<Boolean> SEND_REASON_STRING_KEY = AttributeKey.valueOf("reason.string.send");

    private ChannelAttributes() {}

}
