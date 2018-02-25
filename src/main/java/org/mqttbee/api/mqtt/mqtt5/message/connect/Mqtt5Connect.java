package org.mqttbee.api.mqtt.mqtt5.message.connect;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.Optional;

/**
 * MQTT 5 CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Connect extends Mqtt5Message {

    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_START = true;
    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    long NO_SESSION_EXPIRY = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE;
    boolean DEFAULT_RESPONSE_INFORMATION_REQUESTED = false;
    boolean DEFAULT_PROBLEM_INFORMATION_REQUESTED = true;

    @NotNull
    static Mqtt5ConnectBuilder builder() {
        return new Mqtt5ConnectBuilder();
    }

    @NotNull
    static Mqtt5ConnectBuilder extend(@NotNull final Mqtt5Connect connect) {
        return new Mqtt5ConnectBuilder(connect);
    }

    /**
     * @return the keep alive the client wants to use.
     */
    int getKeepAlive();

    /**
     * @return whether the client has no session present or wants to clear a present session.
     */
    boolean isCleanStart();

    /**
     * @return the session expiry interval the client wants to use. The default is {@link
     * #DEFAULT_SESSION_EXPIRY_INTERVAL}. If it is {@link #NO_SESSION_EXPIRY} the session does not expire.
     */
    long getSessionExpiryInterval();

    /**
     * @return whether the client requests response information from the server. The default is {@link
     * #DEFAULT_RESPONSE_INFORMATION_REQUESTED}.
     */
    boolean isResponseInformationRequested();

    /**
     * @return whether the client requests problem information from the server. The default is {@link
     * #DEFAULT_PROBLEM_INFORMATION_REQUESTED}.
     */
    boolean isProblemInformationRequested();

    /**
     * @return the restrictions set from the client.
     */
    @NotNull
    Mqtt5ConnectRestrictions getRestrictions();

    /**
     * @return the optional simple authentication and/or authorization related data of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt5SimpleAuth> getSimpleAuth();

    /**
     * @return the optional enhanced authentication and/or authorization provider of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt5EnhancedAuthProvider> getEnhancedAuthProvider();

    /**
     * @return the optional Will Publish of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt5WillPublish> getWillPublish();

    /**
     * @return the optional user properties of this CONNECT packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNECT;
    }

}
