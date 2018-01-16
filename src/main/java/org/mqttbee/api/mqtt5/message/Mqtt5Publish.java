package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

import java.util.Optional;

/**
 * MQTT 5 PUBLISH packet.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Publish {

    /**
     * The default message expiry interval which indicates that the message does not expire.
     */
    long DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY = Long.MAX_VALUE;
    /**
     * The default handling for using a topic alias.
     */
    TopicAliasUse DEFAULT_TOPIC_ALIAS_USE = TopicAliasUse.MUST_NOT;

    /**
     * @return the topic of this PUBLISH packet.
     */
    @NotNull
    Mqtt5Topic getTopic();

    /**
     * @return the optional payload of this PUBLISH packet.
     */
    @NotNull
    Optional<byte[]> getPayload();

    /**
     * @return the QoS of this PUBLISH packet.
     */
    @NotNull
    Mqtt5QoS getQos();

    /**
     * @return whether this PUBLISH packet is a retained message.
     */
    boolean isRetain();

    /**
     * @return the message expiry interval of this PUBLISH packet. The default is {@link
     * #DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY}.
     */
    long getMessageExpiryInterval();

    /**
     * @return the optional payload format indicator of this PUBLISH packet.
     */
    @NotNull
    Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator();

    /**
     * @return the optional content type of this PUBLISH packet.
     */
    @NotNull
    Optional<Mqtt5UTF8String> getContentType();

    /**
     * @return the optional response topic of this PUBLISH packet.
     */
    @NotNull
    Optional<Mqtt5Topic> getResponseTopic();

    /**
     * @return the optional correlation data of this PUBLISH packet.
     */
    @NotNull
    Optional<byte[]> getCorrelationData();

    /**
     * @return the handling for using a topic alias.
     */
    @NotNull
    TopicAliasUse getTopicAliasUse();

    /**
     * @return the optional user properties of this PUBLISH packet.
     */
    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();


    /**
     * The handling for using a topic alias.
     */
    enum TopicAliasUse {
        /**
         * Indicates that an outgoing PUBLISH packet must not use a topic alias.
         */
        MUST_NOT,
        /**
         * Indicates that an outgoing PUBLISH packet may use a topic alias.
         */
        MAY,
        /**
         * Indicates that an outgoing PUBLISH packet may use a topic alias and also may overwrite an existing topic
         * alias mapping.
         */
        MAY_OVERWRITE,
        /**
         * Indicates that a incoming PUBLISH packet does not have a topic alias.
         */
        HAS_NOT,
        /**
         * Indicates that a incoming PUBLISH packet has a topic alias.
         */
        HAS
    }

}
