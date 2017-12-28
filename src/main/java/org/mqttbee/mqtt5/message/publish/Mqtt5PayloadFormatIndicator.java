package org.mqttbee.mqtt5.message.publish;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5PayloadFormatIndicator {

    UNSPECIFIED(0),
    UTF_8(1);

    private final int value;

    Mqtt5PayloadFormatIndicator(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
