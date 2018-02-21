package org.mqttbee.mqtt.codec.encoder.mqtt5;

import org.mqttbee.api.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageType;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;

import static org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckEncoder extends
        Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<MqttPubAckImpl, Mqtt5PubAckReasonCode, MqttMessageEncoderProvider<MqttPubAckImpl>> {

    public static final MqttMessageEncoderProvider<MqttPubAckImpl> PROVIDER = Mqtt5PubAckEncoder::new;

    private static final int FIXED_HEADER = MqttMessageType.PUBACK.getCode() << 4;

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubAckReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
