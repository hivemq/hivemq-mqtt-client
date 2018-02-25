package org.mqttbee.mqtt.codec.decoder.mqtt3;

import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;

/**
 * Util for decoders of MQTT 3 messages.
 *
 * @author Silvio Giebl
 */
class Mqtt3MessageDecoderUtil {

    private Mqtt3MessageDecoderUtil() {
    }

    static MqttDecoderException wrongReturnCode() {
        return new MqttDecoderException("wrong return code");
    }

}
