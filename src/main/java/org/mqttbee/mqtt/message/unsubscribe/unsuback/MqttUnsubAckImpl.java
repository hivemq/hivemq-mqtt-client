package org.mqttbee.mqtt.message.unsubscribe.unsuback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCodes;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubAckImpl extends
        MqttMessageWithIdAndReasonCodes<MqttUnsubAckImpl, Mqtt5UnsubAckReasonCode, MqttMessageEncoderProvider<MqttUnsubAckImpl>>
        implements Mqtt5UnsubAck {

    public MqttUnsubAckImpl(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties, null);
    }

    @NotNull
    @Override
    protected MqttUnsubAckImpl getCodable() {
        return this;
    }

}
