package org.mqttbee.mqtt5.message.publish.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithIdAndReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecImpl extends Mqtt5MessageWithIdAndReasonCode<Mqtt5PubRecImpl, Mqtt5PubRecReasonCode>
        implements Mqtt5PubRec, Mqtt5QoSMessage {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    public Mqtt5PubRecImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRecReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubRecImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubRecImpl>> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @Override
    protected Mqtt5PubRecImpl getCodable() {
        return this;
    }

}
