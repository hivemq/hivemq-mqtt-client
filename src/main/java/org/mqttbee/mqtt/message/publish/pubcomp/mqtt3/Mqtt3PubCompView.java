package org.mqttbee.mqtt.message.publish.pubcomp.mqtt3;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.publish.pubcomp.Mqtt3PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3PubCompEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PubCompView implements Mqtt3PubComp {

    private static Mqtt3PubCompView INSTANCE;

    public static MqttPubComp wrapped(final int packetIdentifier) {
        return new MqttPubComp(packetIdentifier, Mqtt5PubCompReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3PubCompEncoder.PROVIDER);
    }

    public static Mqtt3PubCompView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PubCompView();
    }

    private Mqtt3PubCompView() {
    }

}
