package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Connect {

    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    boolean DEFAULT_RESPONSE_INFORMATION_REQUESTED = false;
    boolean DEFAULT_PROBLEM_INFORMATION_REQUESTED = true;

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
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    interface Auth {

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
        int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = Integer.MAX_VALUE;

        int getReceiveMaximum();

        long getTopicAliasMaximum();

        int getMaximumPacketSize();

    }

}
