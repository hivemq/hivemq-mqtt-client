package org.mqttbee.api.mqtt.mqtt3.message.connect;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;

import java.util.Optional;

/**
 * MQTT 3 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt3Connect extends Mqtt3Message {

    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_SESSION = true;

    @NotNull
    static Mqtt3ConnectBuilder builder() {
        return new Mqtt3ConnectBuilder();
    }

    @NotNull
    static Mqtt3ConnectBuilder extend(@NotNull final Mqtt3Connect connect) {
        return new Mqtt3ConnectBuilder(connect);
    }

    /**
     * @return the keep alive the client wants to use.
     */
    int getKeepAlive();

    /**
     * @return whether the client wants a clean session which lasts for the lifetime of the session. A present session
     * is cleared.
     */
    boolean isCleanSession();

    /**
     * @return the optional simple authentication and/or authorization related data of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt3SimpleAuth> getSimpleAuth();

    /**
     * @return the optional Will Publish of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt3Publish> getWillPublish();

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.CONNECT;
    }

}
