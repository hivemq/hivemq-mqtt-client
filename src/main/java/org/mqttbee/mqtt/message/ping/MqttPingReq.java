package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttPingReqEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPingReq implements MqttMessage, Mqtt5PingReq {

    public static final MqttPingReq INSTANCE = new MqttPingReq();

    private MqttPingReq() {
    }

    @NotNull
    @Override
    public MqttMessageEncoder getEncoder() {
        return MqttPingReqEncoder.INSTANCE;
    }

}
