package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublish;

/**
 * @author Silvio Giebl
 */
public class MqttPublishWithFlow {

    private final MqttPublish publish;
    private final MqttIncomingAckFlow incomingAckFlow;

    public MqttPublishWithFlow(
            @NotNull final MqttPublish publish, @NotNull final MqttIncomingAckFlow incomingAckFlow) {

        this.publish = publish;
        this.incomingAckFlow = incomingAckFlow;
    }

    @NotNull
    public MqttPublish getPublish() {
        return publish;
    }

    @NotNull
    public MqttIncomingAckFlow getIncomingAckFlow() {
        return incomingAckFlow;
    }

}
