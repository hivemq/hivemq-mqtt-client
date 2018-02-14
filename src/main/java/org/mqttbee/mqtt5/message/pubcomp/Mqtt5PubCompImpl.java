package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithIdAndReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompImpl extends Mqtt5MessageWithIdAndReasonCode<Mqtt5PubCompImpl, Mqtt5PubCompReasonCode>
        implements Mqtt5PubComp, Mqtt5QoSMessage {

    @NotNull
    public static final Mqtt5PubCompReasonCode DEFAULT_REASON_CODE = Mqtt5PubCompReasonCode.SUCCESS;

    public Mqtt5PubCompImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubCompReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubCompImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubCompImpl>> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @Override
    protected Mqtt5PubCompImpl getCodable() {
        return this;
    }

}
