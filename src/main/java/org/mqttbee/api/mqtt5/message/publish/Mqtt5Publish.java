package org.mqttbee.api.mqtt5.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.Mqtt5Topic;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;

import java.nio.ByteBuffer;
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
    @NotNull
    TopicAliasUsage DEFAULT_TOPIC_ALIAS_USAGE = TopicAliasUsage.MUST_NOT;

    @NotNull
    static Mqtt5PublishBuilder builder() {
        return new Mqtt5PublishBuilder();
    }

    @NotNull
    static Mqtt5PublishBuilder extend(@NotNull final Mqtt5Publish publish) {
        return new Mqtt5PublishBuilder(publish);
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
    Optional<ByteBuffer> getPayload();

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
    Optional<ByteBuffer> getCorrelationData();

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

}
