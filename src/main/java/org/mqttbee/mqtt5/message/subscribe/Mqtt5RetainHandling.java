package org.mqttbee.mqtt5.message.subscribe;

import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5RetainHandling {

    SEND,
    SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
    DONT_SEND;

    public int getCode() {
        return ordinal();
    }

    @Nullable
    public static Mqtt5RetainHandling fromCode(final int code) {
        final Mqtt5RetainHandling[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}
