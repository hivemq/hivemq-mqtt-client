package org.mqttbee.mqtt3.message.suback;

public enum Mqtt3SubAckReasonCode {


    SUCCESS_MAXIMUM_QOS_0(0),
    SUCCESS_MAXIMUM_QOS_1(1),
    SUCCESS_MAXIMUM_QOS_2(2),
    FAILURE(128);


    private final int code;

    Mqtt3SubAckReasonCode(int code) {
        this.code = code;
    }


    public static Mqtt3SubAckReasonCode from(int code) {
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