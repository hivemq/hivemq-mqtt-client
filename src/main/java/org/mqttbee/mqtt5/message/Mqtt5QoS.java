package org.mqttbee.mqtt5.message;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5QoS {

    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    private final int value;

    Mqtt5QoS(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
