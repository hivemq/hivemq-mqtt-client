package org.mqttbee.mqtt5.message.ping;

import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingRespImpl extends Mqtt5Message<Mqtt5PingRespImpl> implements Mqtt5PingResp {

    public static final Mqtt5PingRespImpl INSTANCE = new Mqtt5PingRespImpl();

    private Mqtt5PingRespImpl() {
        super(null);
    }

    @Override
    protected Mqtt5PingRespImpl getCodable() {
        return this;
    }

}
