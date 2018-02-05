package org.mqttbee.mqtt5;

import io.netty.util.AttributeKey;

/**
 * @author Silvio Giebl
 */
public class ChannelAttributes {

    public static final AttributeKey<Boolean> SEND_REASON_STRING = AttributeKey.valueOf("reason.string.send");

    public static final AttributeKey<Boolean> VALIDATE_PAYLOAD_FORMAT =
            AttributeKey.valueOf("payload.format.indicator.validate");

    private ChannelAttributes() {
    }

}
