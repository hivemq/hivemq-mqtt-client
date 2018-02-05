package org.mqttbee.mqtt3.message;

public enum Mqtt3ReasonCode {

    SUCCESS(0x00);

    private final int code;

    Mqtt3ReasonCode(final int code) {
        this.code = code;
    }

    /**
     * @return the byte code of this Reason Code.
     */
    public int getCode() {
        return code;
    }

}
