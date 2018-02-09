package org.mqttbee.mqtt5.message.ping;

import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PingReqEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingReqImpl extends Mqtt5Message<Mqtt5PingReqImpl> implements Mqtt5PingReq {

    public static final Mqtt5PingReqImpl INSTANCE = new Mqtt5PingReqImpl();

    private Mqtt5PingReqImpl() {
        super(Mqtt5PingReqEncoder.PROVIDER);
    }

    @Override
    protected Mqtt5PingReqImpl getCodable() {
        return this;
    }

}
