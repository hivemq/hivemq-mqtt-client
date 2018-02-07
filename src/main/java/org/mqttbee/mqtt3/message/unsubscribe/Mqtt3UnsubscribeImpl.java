package org.mqttbee.mqtt3.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3Unsubscribe;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;

public class Mqtt3UnsubscribeImpl implements Mqtt3Unsubscribe, Mqtt3Message {
    @NotNull
    @Override
    public ImmutableList<Mqtt5TopicFilter> getTopicFilters() {
        return null;
        //TODO
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        //TODO
    }

    @Override
    public int encodedLength() {
        //TODO
        return 0;
    }
}
