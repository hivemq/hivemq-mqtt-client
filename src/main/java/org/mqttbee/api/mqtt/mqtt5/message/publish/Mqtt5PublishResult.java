package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.mqtt.message.publish.MqttPublish;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5PublishResult {

    @NotNull
    MqttPublish getPublish();

    boolean isSuccess();

    Throwable getError();


    interface Mqtt5QoS1Result extends Mqtt5PublishResult {
        @NotNull
        Mqtt5PubAck getPubAck();
    }


    interface Mqtt5QoS2Result extends Mqtt5PublishResult {
        @NotNull
        Mqtt5PubComp getPubComp();
    }

}
