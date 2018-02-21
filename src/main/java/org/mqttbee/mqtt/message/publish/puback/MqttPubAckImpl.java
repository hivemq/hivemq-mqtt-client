package org.mqttbee.mqtt.message.publish.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;

/**
 * @author Silvio Giebl
 */
public class MqttPubAckImpl extends
        MqttMessageWithIdAndReasonCode<MqttPubAckImpl, Mqtt5PubAckReasonCode, MqttMessageEncoderProvider<MqttPubAckImpl>>
        implements Mqtt5PubAck, MqttQoSMessage {

    @NotNull
    public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    public MqttPubAckImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubAckReasonCode reasonCode,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttMessageEncoderProvider<MqttPubAckImpl> encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBACK;
    }

    @NotNull
    @Override
    protected MqttPubAckImpl getCodable() {
        return this;
    }

}
