package org.mqttbee.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5UnsubscribeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeImpl extends Mqtt5Message.Mqtt5MessageWithUserProperties implements Mqtt5Unsubscribe {

    private final ImmutableList<Mqtt5TopicFilterImpl> topicFilters;

    public Mqtt5UnsubscribeImpl(
            @NotNull final ImmutableList<Mqtt5TopicFilterImpl> topicFilters,
            @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        super(userProperties);
        this.topicFilters = topicFilters;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5TopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5UnsubscribeEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5UnsubscribeEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
