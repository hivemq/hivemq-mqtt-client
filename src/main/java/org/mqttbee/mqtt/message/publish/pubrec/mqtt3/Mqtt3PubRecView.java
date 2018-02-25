package org.mqttbee.mqtt.message.publish.pubrec.mqtt3;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.publish.pubrec.Mqtt3PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3PubRecEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PubRecView implements Mqtt3PubRec {

    private static Mqtt3PubRecView INSTANCE;

    public static MqttPubRecImpl wrapped(final int packetIdentifier) {
        return new MqttPubRecImpl(packetIdentifier, Mqtt5PubRecReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3PubRecEncoder.PROVIDER);
    }

    public static Mqtt3PubRecView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PubRecView();
    }

    private Mqtt3PubRecView() {
    }

}
