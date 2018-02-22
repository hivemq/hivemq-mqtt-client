package org.mqttbee.mqtt.message.connect.connack.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;

import static org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnAckView implements Mqtt3ConnAck {

    @NotNull
    public static MqttConnAckImpl wrapped(
            @NotNull final Mqtt3ConnAckReturnCode returnCode, final boolean isSessionPresent) {

        return new MqttConnAckImpl(wrappedReasonCode(returnCode), isSessionPresent,
                MqttConnAckImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, MqttConnAckImpl.KEEP_ALIVE_FROM_CONNECT,
                MqttConnAckImpl.CLIENT_IDENTIFIER_FROM_CONNECT, null, MqttConnAckImpl.RestrictionsImpl.DEFAULT, null,
                null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    private static Mqtt5ConnAckReasonCode wrappedReasonCode(@NotNull final Mqtt3ConnAckReturnCode returnCode) {
        switch (returnCode) {
            case SUCCESS:
                return Mqtt5ConnAckReasonCode.SUCCESS;
            case UNSUPPORTED_PROTOCOL_VERSION:
                return Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION;
            case IDENTIFIER_REJECTED:
                return Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;
            case SERVER_UNAVAILABLE:
                return Mqtt5ConnAckReasonCode.SERVER_UNAVAILABLE;
            case BAD_USER_NAME_OR_PASSWORD:
                return Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
            case NOT_AUTHORIZED:
                return Mqtt5ConnAckReasonCode.NOT_AUTHORIZED;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    private static Mqtt3ConnAckReturnCode wrapReasonCode(@NotNull final Mqtt5ConnAckReasonCode reasonCode) {
        switch (reasonCode) {
            case SUCCESS:
                return SUCCESS;
            case UNSUPPORTED_PROTOCOL_VERSION:
                return UNSUPPORTED_PROTOCOL_VERSION;
            case CLIENT_IDENTIFIER_NOT_VALID:
                return IDENTIFIER_REJECTED;
            case SERVER_UNAVAILABLE:
                return SERVER_UNAVAILABLE;
            case BAD_USER_NAME_OR_PASSWORD:
                return BAD_USER_NAME_OR_PASSWORD;
            case NOT_AUTHORIZED:
                return NOT_AUTHORIZED;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    public static Mqtt3ConnAckView create(
            @NotNull final Mqtt3ConnAckReturnCode returnCode, final boolean isSessionPresent) {

        return new Mqtt3ConnAckView(wrapped(returnCode, isSessionPresent));
    }

    private final MqttConnAckImpl wrapped;

    private Mqtt3ConnAckView(@NotNull final MqttConnAckImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public Mqtt3ConnAckReturnCode getReturnCode() {
        return wrapReasonCode(wrapped.getReasonCode());
    }

    @Override
    public boolean isSessionPresent() {
        return wrapped.isSessionPresent();
    }

    @NotNull
    public MqttConnAckImpl getWrapped() {
        return wrapped;
    }

}
