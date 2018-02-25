package org.mqttbee.mqtt.message.publish.pubrec;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRecEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPubRec
        extends MqttMessageWithIdAndReasonCode<MqttPubRec, Mqtt5PubRecReasonCode, MqttPubRecEncoderProvider>
        implements Mqtt5PubRec, MqttQoSMessage {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    public MqttPubRec(
            final int packetIdentifier, @NotNull final Mqtt5PubRecReasonCode reasonCode,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttPubRecEncoderProvider encoderProvider) {

        super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
    }

//    public MqttPubRel ack() {
//        return new MqttPubRel(getPacketIdentifier(), Mqtt5PubRelReasonCode.SUCCESS, null,
//                MqttUserPropertiesImpl.NO_USER_PROPERTIES, encoderProvider.getPubRelEncoderProvider());
//    }

    @NotNull
    @Override
    protected MqttPubRec getCodable() {
        return this;
    }

}
