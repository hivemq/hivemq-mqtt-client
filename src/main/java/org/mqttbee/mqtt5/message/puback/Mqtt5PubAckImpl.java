package org.mqttbee.mqtt5.message.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithIdAndReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckImpl extends Mqtt5MessageWithIdAndReasonCode<Mqtt5PubAckImpl, Mqtt5PubAckReasonCode>
        implements Mqtt5PubAck, Mqtt5QoSMessage {

    @NotNull
    public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    public Mqtt5PubAckImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubAckReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubAckImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubAckImpl>> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @Override
    protected Mqtt5PubAckImpl getCodable() {
        return this;
    }

}
