package org.mqttbee.mqtt.message.subscribe.suback;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCodes;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubAck extends
        MqttMessageWithIdAndReasonCodes<MqttSubAck, Mqtt5SubAckReasonCode, MqttMessageEncoderProvider<MqttSubAck>>
        implements Mqtt5SubAck {

    public MqttSubAck(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties, null);
    }

    @NotNull
    @Override
    protected MqttSubAck getCodable() {
        return this;
    }

}
