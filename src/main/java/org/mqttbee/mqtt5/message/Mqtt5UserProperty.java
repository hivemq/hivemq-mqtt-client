package org.mqttbee.mqtt5.message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserProperty {

    private final String name;
    private final String value;

    public Mqtt5UserProperty(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
