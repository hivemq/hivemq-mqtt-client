package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttPingResp implements MqttMessage, Mqtt5PingResp {

    public static final MqttPingResp INSTANCE = new MqttPingResp();

    private MqttPingResp() {
    }

    @NotNull
    @Override
    public MqttMessageEncoder getEncoder() {
        throw new UnsupportedOperationException();
    }

}
