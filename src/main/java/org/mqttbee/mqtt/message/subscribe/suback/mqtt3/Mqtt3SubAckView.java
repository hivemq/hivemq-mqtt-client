package org.mqttbee.mqtt.message.subscribe.suback.mqtt3;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAckImpl;

import static org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubAckView implements Mqtt3SubAck {

    private final MqttSubAckImpl wrapped;

    @NotNull
    public static MqttSubAckImpl wrapped(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        return new MqttSubAckImpl(
                packetIdentifier, wrappedReturnCodes(returnCodes), null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    private static ImmutableList<Mqtt5SubAckReasonCode> wrappedReturnCodes(
            @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        final ImmutableList.Builder<Mqtt5SubAckReasonCode> builder =
                ImmutableList.builderWithExpectedSize(returnCodes.size());
        for (int i = 0; i < returnCodes.size(); i++) {
            builder.add(wrappedReturnCode(returnCodes.get(i)));
        }
        return builder.build();
    }

    @NotNull
    private static Mqtt5SubAckReasonCode wrappedReturnCode(@NotNull final Mqtt3SubAckReturnCode returnCode) {
        switch (returnCode) {
            case SUCCESS_MAXIMUM_QOS_0:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_0;
            case SUCCESS_MAXIMUM_QOS_1:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_1;
            case SUCCESS_MAXIMUM_QOS_2:
                return Mqtt5SubAckReasonCode.GRANTED_QOS_2;
            case FAILURE:
                return Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    private static ImmutableList<Mqtt3SubAckReturnCode> wrapReasonCodes(
            @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes) {

        final ImmutableList.Builder<Mqtt3SubAckReturnCode> builder =
                ImmutableList.builderWithExpectedSize(reasonCodes.size());
        for (int i = 0; i < reasonCodes.size(); i++) {
            builder.add(wrapReasonCode(reasonCodes.get(i)));
        }
        return builder.build();
    }

    @NotNull
    private static Mqtt3SubAckReturnCode wrapReasonCode(@NotNull final Mqtt5SubAckReasonCode reasonCode) {
        switch (reasonCode) {
            case GRANTED_QOS_0:
                return SUCCESS_MAXIMUM_QOS_0;
            case GRANTED_QOS_1:
                return SUCCESS_MAXIMUM_QOS_1;
            case GRANTED_QOS_2:
                return SUCCESS_MAXIMUM_QOS_2;
            case UNSPECIFIED_ERROR:
                return FAILURE;
            default:
                throw new IllegalStateException();
        }
    }

    @NotNull
    public static Mqtt3SubAckView create(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt3SubAckReturnCode> returnCodes) {

        return new Mqtt3SubAckView(wrapped(packetIdentifier, returnCodes));
    }

    private Mqtt3SubAckView(@NotNull final MqttSubAckImpl wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt3SubAckReturnCode> getReturnCodes() {
        return wrapReasonCodes(wrapped.getReasonCodes());
    }

    @NotNull
    public MqttSubAckImpl getWrapped() {
        return wrapped;
    }

}
