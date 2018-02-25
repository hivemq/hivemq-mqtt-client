package org.mqttbee.mqtt.message.ping.mqtt3;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.ping.Mqtt3PingReq;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PingReqView implements Mqtt3PingReq {

    private static Mqtt3PingReqView INSTANCE;

    public static Mqtt3PingReqView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3PingReqView();
    }

    private Mqtt3PingReqView() {
    }

}
