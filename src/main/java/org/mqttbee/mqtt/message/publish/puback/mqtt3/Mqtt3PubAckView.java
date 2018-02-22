package org.mqttbee.mqtt.message.publish.puback.mqtt3;

import org.mqttbee.api.mqtt.mqtt3.message.publish.puback.Mqtt3PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3PubAckEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PubAckView implements Mqtt3PubAck {

    private static Mqtt3PubAckView INSTANCE;

    public static MqttPubAckImpl wrapped(final int packetIdentifier) {
        return new MqttPubAckImpl(packetIdentifier, Mqtt5PubAckReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3PubAckEncoder.PROVIDER);
    }

    public static Mqtt3PubAckView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PubAckView();
    }

    private Mqtt3PubAckView() {
    }

}
