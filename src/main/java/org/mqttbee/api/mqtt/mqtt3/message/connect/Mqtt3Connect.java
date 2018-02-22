package org.mqttbee.api.mqtt.mqtt3.message.connect;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * MQTT 3 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt3Connect extends Mqtt3Message {

    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_SESSION = true;

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
    Optional<SimpleAuth> getSimpleAuth();

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

    /**
     * Simple authentication and/or authorization related data in the CONNECT packet.
     */
    @DoNotImplement
    interface SimpleAuth {

        /**
         * @return the username.
         */
        @NotNull
        MqttUTF8String getUsername();

        /**
         * @return the optional password.
         */
        @NotNull
        Optional<ByteBuffer> getPassword();

    }

}
