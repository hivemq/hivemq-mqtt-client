package org.mqttbee.mqtt.message.publish.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;

/**
 * @author Silvio Giebl
 */
public class MqttPubCompImpl extends
        MqttMessageWithIdAndReasonCode<MqttPubCompImpl, Mqtt5PubCompReasonCode, MqttMessageEncoderProvider<MqttPubCompImpl>>
        implements Mqtt5PubComp, MqttQoSMessage {

    @NotNull
    public static final Mqtt5PubCompReasonCode DEFAULT_REASON_CODE = Mqtt5PubCompReasonCode.SUCCESS;

    public MqttPubCompImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubCompReasonCode reasonCode,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttMessageEncoderProvider<MqttPubCompImpl> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBCOMP;
    }

    @NotNull
    @Override
    protected MqttPubCompImpl getCodable() {
        return this;
    }

}
