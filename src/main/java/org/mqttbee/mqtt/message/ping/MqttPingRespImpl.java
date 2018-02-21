package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * @author Silvio Giebl
 */
public class MqttPingRespImpl implements MqttMessage, Mqtt5PingResp {

    public static final MqttPingRespImpl INSTANCE = new MqttPingRespImpl();

    private MqttPingRespImpl() {
    }

    @NotNull
    @Override
    public MqttMessageEncoder getEncoder() {
        throw new UnsupportedOperationException();
    }

}
