package org.mqttbee.mqtt.codec.encoder.mqtt5;

import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRelEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageType;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

import static org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelEncoder extends
        Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<MqttPubRelImpl, Mqtt5PubRelReasonCode, MqttPubRelEncoderProvider> {

    public static final MqttPubRelEncoderProvider PROVIDER =
            new MqttPubRelEncoderProvider(Mqtt5PubRelEncoder::new, Mqtt5PubCompEncoder.PROVIDER);

    private static final int FIXED_HEADER = (MqttMessageType.PUBREL.getCode() << 4) | 0b0010;

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubRelReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
