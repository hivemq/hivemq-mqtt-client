package org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback;

import org.mqttbee.annotations.Nullable;

/**
 * SUBACK Return Code according to the MQTT 3.1.1 specification.
 */
public enum Mqtt3SubAckReturnCode {

    SUCCESS_MAXIMUM_QOS_0(0),
    SUCCESS_MAXIMUM_QOS_1(1),
    SUCCESS_MAXIMUM_QOS_2(2),
    FAILURE(128);

    private final int code;

    Mqtt3SubAckReturnCode(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static Mqtt3SubAckReturnCode from(final int code) {
        switch (code) {
            case 0:
                return SUCCESS_MAXIMUM_QOS_0;
            case 1:
                return SUCCESS_MAXIMUM_QOS_1;
            case 2:
                return SUCCESS_MAXIMUM_QOS_2;
            case 128:
                return FAILURE;
            default:
                return null;
        }
    }

}