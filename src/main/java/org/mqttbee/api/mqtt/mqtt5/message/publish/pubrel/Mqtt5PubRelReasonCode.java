package org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.message.MqttCommonReasonCode;

/**
 * MQTT Reason Codes that can be used in PUBREL packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PubRelReasonCode implements Mqtt5ReasonCode {

    SUCCESS(MqttCommonReasonCode.SUCCESS),
    PACKET_IDENTIFIER_NOT_FOUND(MqttCommonReasonCode.PACKET_IDENTIFIER_NOT_FOUND);

    private final int code;

    Mqtt5PubRelReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubRelReasonCode(@NotNull final MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this PUBREL Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the PUBREL Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PUBREL Reason Code belonging to the given byte code or null if the byte code is not a valid PUBREL
     * Reason Code code.
     */
    @Nullable
    public static Mqtt5PubRelReasonCode fromCode(final int code) {
        for (final Mqtt5PubRelReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}
