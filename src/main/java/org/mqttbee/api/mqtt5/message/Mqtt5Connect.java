package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.Optional;

/**
 * MQTT 5 CONNECT packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Connect {

    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    long NO_SESSION_EXPIRY = 0xFFFF_FFFF;
    boolean DEFAULT_RESPONSE_INFORMATION_REQUESTED = false;
    boolean DEFAULT_PROBLEM_INFORMATION_REQUESTED = true;

    /**
     * @return the optional client identifier of this CONNECT packet. If absent, the server may assign a client
     * identifier and return it in the corresponding CONNACK packet.
     */
    @NotNull
    Optional<Mqtt5ClientIdentifier> getClientIdentifier();

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
     * #DEFAULT_SESSION_EXPIRY_INTERVAL}. If is is {@link #NO_SESSION_EXPIRY} the session does not expire.
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
    Restrictions getRestrictions();

    /**
     * @return the optional authentication and/or authorization related data of this CONNECT packet.
     */
    @NotNull
    Optional<Auth> getAuth();

    /**
     * @return the optional Will Publish of this CONNECT packet.
     */
    @NotNull
    Optional<Mqtt5WillPublishImpl> getWillPublish();

    /**
     * @return the optional user properties of this CONNECT packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    /**
     * Authentication and/or authorization related data in the CONNECT packet.
     */
    interface Auth {

        /**
         * @return the optional username.
         */
        @NotNull
        Optional<Mqtt5UTF8String> getUsername();

        /**
         * @return the optional password.
         */
        @NotNull
        Optional<byte[]> getPassword();

        /**
         * @return the authentication/authorization method.
         */
        @NotNull
        Optional<Mqtt5UTF8String> getMethod();

        /**
         * @return the optional authentication/authorization data.
         */
        @NotNull
        Optional<byte[]> getData();

    }


    /**
     * Restrictions from the the server in the CONNECT packet.
     */
    interface Restrictions {

        /**
         * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts concurrently.
         */
        int DEFAULT_RECEIVE_MAXIMUM = 65_535;
        /**
         * The default maximum amount of topic aliases the client accepts from the server.
         */
        long DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
        /**
         * The default maximum packet size the client accepts from the server which indicates that the packet size is
         * not limited beyond the restrictions of the encoding.
         */
        int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = Integer.MAX_VALUE;

        /**
         * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts concurrently. The
         * default is {@link #DEFAULT_RECEIVE_MAXIMUM}.
         */
        int getReceiveMaximum();

        /**
         * @return the maximum amount of topic aliases the client accepts from the server. The default is {@link
         * #DEFAULT_TOPIC_ALIAS_MAXIMUM}.
         */
        long getTopicAliasMaximum();

        /**
         * @return the maximum packet size the client accepts from the server. The default is {@link
         * #DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT}.
         */
        int getMaximumPacketSize();

    }

}
