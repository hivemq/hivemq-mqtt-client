package org.mqttbee.mqtt5.message.subscribe;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5RetainHandling {

    SEND(0),
    SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST(1),
    DONT_SEND(2);

    private final int value;

    Mqtt5RetainHandling(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
