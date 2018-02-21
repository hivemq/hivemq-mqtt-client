package org.mqttbee.mqtt.codec.encoder.mqtt5;

import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPubRecEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageType;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;

import static org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecEncoder extends
        Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<MqttPubRecImpl, Mqtt5PubRecReasonCode, MqttPubRecEncoderProvider> {

    public static final MqttPubRecEncoderProvider PROVIDER =
            new MqttPubRecEncoderProvider(Mqtt5PubRecEncoder::new, Mqtt5PubRelEncoder.PROVIDER);

    private static final int FIXED_HEADER = MqttMessageType.PUBREC.getCode() << 4;

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubRecReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
