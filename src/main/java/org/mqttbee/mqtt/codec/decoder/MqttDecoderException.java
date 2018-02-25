package org.mqttbee.mqtt.codec.decoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * @author Silvio Giebl
 */
public class MqttDecoderException extends Exception {

    private final Mqtt5DisconnectReasonCode reasonCode;
    private Mqtt5MessageType messageType;

    public MqttDecoderException(@NotNull final Mqtt5DisconnectReasonCode reasonCode, @NotNull final String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public MqttDecoderException(@NotNull final String message) {
        super(message);
        this.reasonCode = Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public void setMessageType(@Nullable final Mqtt5MessageType messageType) {
        this.messageType = messageType;
    }

    @NotNull
    public Mqtt5DisconnectReasonCode getReasonCode() {
        return reasonCode;
    }

    @Override
    public String getMessage() {
        return "Decoder exception for " + ((messageType == null) ? "UNKNOWN" : messageType) + ": " + super.getMessage();
    }

}
