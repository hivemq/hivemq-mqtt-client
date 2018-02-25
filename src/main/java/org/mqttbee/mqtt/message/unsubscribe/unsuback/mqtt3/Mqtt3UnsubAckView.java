package org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3UnsubAckView implements Mqtt3UnsubAck {

    private static Mqtt3UnsubAckView INSTANCE;

    @NotNull
    public static MqttUnsubAck wrapped(final int packetIdentifier) {
        return new MqttUnsubAck(
                packetIdentifier, ImmutableList.of(), null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static Mqtt3UnsubAckView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3UnsubAckView();
    }

}
