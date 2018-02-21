package org.mqttbee.mqtt.message.ping;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PingReqEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * @author Silvio Giebl
 */
public class MqttPingReqImpl extends MqttMessage<MqttPingReqImpl, MqttMessageEncoderProvider<MqttPingReqImpl>>
        implements Mqtt5PingReq {

    public static final MqttPingReqImpl INSTANCE = new MqttPingReqImpl();

    private MqttPingReqImpl() {
        super(Mqtt5PingReqEncoder.PROVIDER);
    }

    @NotNull
    @Override
    protected MqttPingReqImpl getCodable() {
        return this;
    }

}
