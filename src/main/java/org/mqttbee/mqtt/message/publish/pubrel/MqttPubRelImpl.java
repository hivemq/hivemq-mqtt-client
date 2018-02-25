package org.mqttbee.mqtt.message.publish.pubrel;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRelEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPubRelImpl
        extends MqttMessageWithIdAndReasonCode<MqttPubRelImpl, Mqtt5PubRelReasonCode, MqttPubRelEncoderProvider>
        implements Mqtt5PubRel, MqttQoSMessage {

    @NotNull
    public static final Mqtt5PubRelReasonCode DEFAULT_REASON_CODE = Mqtt5PubRelReasonCode.SUCCESS;

    public MqttPubRelImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRelReasonCode reasonCode,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttPubRelEncoderProvider encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

    @NotNull
    @Override
    protected MqttPubRelImpl getCodable() {
        return this;
    }

}
