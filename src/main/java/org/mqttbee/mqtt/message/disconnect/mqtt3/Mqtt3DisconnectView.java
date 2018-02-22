package org.mqttbee.mqtt.message.disconnect.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.disconnect.Mqtt3Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3DisconnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt3DisconnectView implements Mqtt3Disconnect {

    private static MqttDisconnectImpl WRAPPED;
    private static Mqtt3DisconnectView INSTANCE;

    @NotNull
    public static MqttDisconnectImpl wrapped() {
        if (WRAPPED != null) {
            return WRAPPED;
        }
        return WRAPPED = new MqttDisconnectImpl(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION,
                MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt3DisconnectEncoder.PROVIDER);
    }

    @NotNull
    public static Mqtt3DisconnectView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3DisconnectView();
    }

    private Mqtt3DisconnectView() {
    }

    @NotNull
    public MqttDisconnectImpl getWrapped() {
        return WRAPPED;
    }

}
