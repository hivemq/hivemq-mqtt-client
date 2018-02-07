package org.mqttbee.api.mqtt5.message;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

import java.util.Optional;

/**
 * MQTT 5 PUBLISH packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Publish {

    /**
     * The default handling for using a topic alias.
     */
    TopicAliasUsage DEFAULT_TOPIC_ALIAS_USAGE = TopicAliasUsage.MUST_NOT;

    static Mqtt5PublishBuilder builder() {
        return new Mqtt5PublishBuilder();
    }

    /**
     * @return the topic of this PUBLISH packet.
     */
    @NotNull
    Mqtt5Topic getTopic();

    /**
     * @return the optional payload of this PUBLISH packet.
     */
    @NotNull
    Optional<ByteBuf> getPayload();

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
     * @return the optional message expiry interval of this PUBLISH packet.
     */
    @NotNull
    Optional<Long> getMessageExpiryInterval();

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
    Optional<ByteBuf> getCorrelationData();

    /**
     * @return the handling for using a topic alias.
     */
    @NotNull
    TopicAliasUsage getTopicAliasUsage();

    /**
     * @return the optional user properties of this PUBLISH packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();


    /**
     * The handling for using a topic alias.
     */
    enum TopicAliasUsage {
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
         * Indicates that an incoming PUBLISH packet does not have a topic alias.
         */
        HAS_NOT,
        /**
         * Indicates that an incoming PUBLISH packet has a topic alias.
         */
        HAS
    }

}
