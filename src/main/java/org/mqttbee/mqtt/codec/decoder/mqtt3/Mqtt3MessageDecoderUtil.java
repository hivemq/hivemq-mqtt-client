package org.mqttbee.mqtt.codec.decoder.mqtt3;

import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;

/**
 * @author Silvio Giebl
 */
class Mqtt3MessageDecoderUtil {

    private Mqtt3MessageDecoderUtil() {
    }

    static MqttDecoderException wrongReturnCode() {
        return new MqttDecoderException("wrong return code");
    }

}
