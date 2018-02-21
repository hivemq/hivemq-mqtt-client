package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * @author Silvio Giebl
 */
public class MqttPingRespImpl extends MqttMessage<MqttPingRespImpl, MqttMessageEncoderProvider<MqttPingRespImpl>>
        implements Mqtt5PingResp {

    public static final MqttPingRespImpl INSTANCE = new MqttPingRespImpl();

    private MqttPingRespImpl() {
        super(null);
    }

    @NotNull
    @Override
    protected MqttPingRespImpl getCodable() {
        return this;
    }

}
