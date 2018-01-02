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
 * @author Silvio Giebl
 */
public interface Mqtt5Publish {

    long DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY = Long.MAX_VALUE;
    int DEFAULT_NO_TOPIC_ALIAS = -1;

    @NotNull
    Mqtt5Topic getTopic();

    @NotNull
    Optional<byte[]> getPayload();

    @NotNull
    Mqtt5QoS getQos();

    boolean isRetain();

    long getMessageExpiryInterval();

    @NotNull
    Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator();

    @NotNull
    Optional<Mqtt5UTF8String> getContentType();

    @NotNull
    Optional<Mqtt5UTF8String> getResponseTopic();

    @NotNull
    Optional<byte[]> getCorrelationData();

    @NotNull
    ImmutableList<Mqtt5UserProperty> getUserProperties();

}
