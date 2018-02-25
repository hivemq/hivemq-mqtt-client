package org.mqttbee.mqtt.message.ping.mqtt3;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.ping.Mqtt3PingResp;

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
