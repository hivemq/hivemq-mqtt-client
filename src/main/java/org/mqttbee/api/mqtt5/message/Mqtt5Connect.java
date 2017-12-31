package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Connect extends Mqtt5Message {

    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    boolean DEFAULT_RESPONSE_INFORMATION_REQUESTED = false;
    int NOT_DEFAULT_RESPONSE_INFORMATION_REQUESTED = DEFAULT_RESPONSE_INFORMATION_REQUESTED ? 0 : 1;
    boolean DEFAULT_PROBLEM_INFORMATION_REQUESTED = true;
    int NOT_DEFAULT_PROBLEM_INFORMATION_REQUESTED = DEFAULT_PROBLEM_INFORMATION_REQUESTED ? 0 : 1;

    @NotNull
    Mqtt5ClientIdentifier getClientIdentifier();

    int getKeepAlive();

    boolean isCleanStart();

    long getSessionExpiryInterval();

    boolean isResponseInformationRequested();

    boolean isProblemInformationRequested();

    @NotNull
    Restrictions getRestrictions();

    @NotNull
    Optional<Auth> getAuth();

    @NotNull
    Optional<Mqtt5WillPublishImpl> getWillPublish();

    @NotNull
    List<Mqtt5UserProperty> getUserProperties();


    interface Auth {

        @Nullable
        Auth DEFAULT_NO_AUTH = null;

        @NotNull
        Optional<Mqtt5UTF8String> getUsername();

        @NotNull
        Optional<byte[]> getPassword();

        @NotNull
        Optional<Mqtt5UTF8String> getMethod();

        @NotNull
        Optional<byte[]> getData();

    }


    interface Restrictions {

        int DEFAULT_RECEIVE_MAXIMUM = 65_535;
        long DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
        int DEFAULT_MAXIMUM_PACKET_SIZE_INFINITY = Integer.MAX_VALUE;
        @NotNull
        Restrictions DEFAULT = new Mqtt5ConnectImpl.RestrictionsImpl(
                DEFAULT_RECEIVE_MAXIMUM, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_INFINITY);

        int getReceiveMaximum();

        long getTopicAliasMaximum();

        int getMaximumPacketSize();

    }

}
