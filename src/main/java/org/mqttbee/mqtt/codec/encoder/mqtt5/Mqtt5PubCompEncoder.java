package org.mqttbee.mqtt.codec.encoder.mqtt5;

import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

import static org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompEncoder extends
        Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<MqttPubComp, Mqtt5PubCompReasonCode, MqttMessageEncoderProvider<MqttPubComp>> {

    public static final MqttMessageEncoderProvider<MqttPubComp> PROVIDER =
            new ThreadLocalMqttMessageEncoderProvider<>(Mqtt5PubCompEncoder::new);

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubCompReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
