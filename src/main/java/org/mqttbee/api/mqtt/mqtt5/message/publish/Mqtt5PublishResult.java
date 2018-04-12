package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PublishResult {

    @NotNull
    Mqtt5Publish getPublish();

    boolean isSuccess();

    @Nullable
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
