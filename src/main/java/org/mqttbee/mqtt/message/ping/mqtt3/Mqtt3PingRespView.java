package org.mqttbee.mqtt.message.ping.mqtt3;

import org.mqttbee.api.mqtt.mqtt3.message.ping.Mqtt3PingResp;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PingRespView implements Mqtt3PingResp {

    private static Mqtt3PingRespView INSTANCE;

    public static Mqtt3PingRespView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PingRespView();
    }

    private Mqtt3PingRespView() {
    }

}
