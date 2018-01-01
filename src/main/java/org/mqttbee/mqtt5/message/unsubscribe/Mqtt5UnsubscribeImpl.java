package org.mqttbee.mqtt5.message.unsubscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Unsubscribe;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Collections;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeImpl implements Mqtt5Unsubscribe {

    private final List<Mqtt5TopicFilter> topicFilters;
    private final List<Mqtt5UserProperty> userProperties;

    public Mqtt5UnsubscribeImpl(
            @NotNull final List<Mqtt5TopicFilter> topicFilters, @NotNull final List<Mqtt5UserProperty> userProperties) {
        this.topicFilters = Collections.unmodifiableList(topicFilters);
        this.userProperties = Collections.unmodifiableList(userProperties);
    }

    @NotNull
    @Override
    public List<Mqtt5TopicFilter> getTopicFilters() {
        return topicFilters;
    }

    @NotNull
    @Override
    public List<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBSCRIBE;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getUnsubscribeEncoder().encode(this, channel, out);
    }

}
