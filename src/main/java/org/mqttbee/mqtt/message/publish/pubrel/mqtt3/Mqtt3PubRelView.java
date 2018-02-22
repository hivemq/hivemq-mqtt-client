package org.mqttbee.mqtt.message.publish.pubrel.mqtt3;

import org.mqttbee.api.mqtt.mqtt3.message.publish.pubrel.Mqtt3PubRel;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3PubRelEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PubRelView implements Mqtt3PubRel {

    private static Mqtt3PubRelView INSTANCE;

    public static MqttPubRelImpl wrapped(final int packetIdentifier) {
        return new MqttPubRelImpl(packetIdentifier, Mqtt5PubRelReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3PubRelEncoder.PROVIDER);
    }

    public static Mqtt3PubRelView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PubRelView();
    }

    private Mqtt3PubRelView() {
    }

}
