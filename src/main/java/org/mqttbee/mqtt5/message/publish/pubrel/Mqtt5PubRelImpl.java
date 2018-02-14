package org.mqttbee.mqtt5.message.publish.pubrel;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.mqttbee.api.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithIdAndReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5QoSMessage;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelImpl extends Mqtt5MessageWithIdAndReasonCode<Mqtt5PubRelImpl, Mqtt5PubRelReasonCode>
        implements Mqtt5PubRel, Mqtt5QoSMessage {

    @NotNull
    public static final Mqtt5PubRelReasonCode DEFAULT_REASON_CODE = Mqtt5PubRelReasonCode.SUCCESS;

    public Mqtt5PubRelImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRelReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubRelImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubRelImpl>> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @Override
    protected Mqtt5PubRelImpl getCodable() {
        return this;
    }

}
